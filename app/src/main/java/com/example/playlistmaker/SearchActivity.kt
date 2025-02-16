package com.example.playlistmaker

import android.annotation.SuppressLint
import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityOptionsCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

open class SearchActivity : BaseActivity() {

    companion object {const val SEARCH_QUERY_KEY = "searchQuery"}

    private lateinit var backButton: ImageView
    private lateinit var searchLayout: LinearLayout
    private lateinit var inputEditText: TextInputEditText
    private lateinit var clearIcon: ImageView
    private lateinit var searchInputLayout: TextInputLayout
    private var searchQuery = ""
    private lateinit var toolbar: Toolbar
    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var bottomNavigationLine: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        enableEdgeToEdge()
        setContentView(R.layout.activity_search)
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        initViews()
        handleWindowInsets()
        setupListeners()
        bottomNavigationView()
        hideBottomNavigationView()

        val tracks = ArrayList<Track>()
        tracks.add(Track("Smells Like Teen Spirit", "Nirvana", "5:01",
            "https://is5-ssl.mzstatic.com/image/thumb/Music115/v4/7b/58/c2/7b58c21a-2b51-2bb2-e59a-9bb9b96ad8c3/00602567924166.rgb.jpg/100x100bb.jpg"))

        tracks.add(Track("Billie Jean","Michael Jackson","4:35",
            "https://is5-ssl.mzstatic.com/image/thumb/Music125/v4/3d/9d/38/3d9d3811-71f0-3a0e-1ada-3004e56ff852/827969428726.jpg/100x100bb.jpg"))

        tracks.add(Track("Stayin' Alive","Bee Gees","4:10",
            "https://is4-ssl.mzstatic.com/image/thumb/Music115/v4/1f/80/1f/1f801fc1-8c0f-ea3e-d3e5-387c6619619e/16UMGIM86640.rgb.jpg/100x100bb.jpg"))

        tracks.add(Track("Whole Lotta Love","Led Zeppelin","5:33",
            "https://is2-ssl.mzstatic.com/image/thumb/Music62/v4/7e/17/e3/7e17e33f-2efa-2a36-e916-7f808576cf6b/mzm.fyigqcbs.jpg/100x100bb.jpg"))

        tracks.add(Track("Sweet Child O'Mine","Guns N' Roses","5:03",
            "https://is5-ssl.mzstatic.com/image/thumb/Music125/v4/a0/4d/c4/a04dc484-03cc-02aa-fa82-5334fcb4bc16/18UMGIM24878.rgb.jpg/100x100bb.jpg"))

        val recyclerView = findViewById<RecyclerView>(R.id.tracks_recycler_view)
        val adapter = TrackAdapter(tracks)
        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        recyclerView.adapter = adapter
    }

    override fun onSegment4Clicked() {
        bottomNavigationView.visibility = View.VISIBLE
        bottomNavigationLine.visibility = View.VISIBLE
    }

    private fun hideBottomNavigationView() {
        bottomNavigationView.visibility = View.GONE
        bottomNavigationLine.visibility = View.GONE
    }

    private fun startActivityWithAnimation(targetActivity: Class<*>) {
        val intent = Intent(this, targetActivity)
        val options = ActivityOptions.makeCustomAnimation(this, R.anim.enter_from_bottom, R.anim.exit_to_top)
        startActivity(intent, options.toBundle())
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }

    private fun initViews() {
        backButton = findViewById(R.id.backArrow)
        searchLayout = findViewById(R.id.activity_search)
        inputEditText = findViewById(R.id.inputEditText)
        clearIcon = findViewById(R.id.clearIcon)
        searchInputLayout = findViewById(R.id.search_box)
        bottomNavigationLine = findViewById(R.id.navigationLine)
    }

    private fun setupListeners() {
        backButton.setOnClickListener {
            ActivityOptionsCompat.makeCustomAnimation(
                this, R.anim.enter_from_left, R.anim.exit_to_right
            ).toBundle()

            finishAfterTransition()
        }

        inputEditText.addTextChangedListener(createTextWatcher())
        clearIcon.setOnClickListener { clearSearchInput() }

        inputEditText.setOnFocusChangeListener { _, hasFocus ->
            searchInputLayout.hint = if (hasFocus) null else getString(R.string.search)
        }
    }

    private fun createTextWatcher(): TextWatcher {
        return object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                searchQuery = s.toString()
                clearIcon.visibility = if (!s.isNullOrEmpty()) View.VISIBLE else View.GONE
            }
            override fun afterTextChanged(s: Editable?) {}
        }
    }

    private fun clearSearchInput() {
        inputEditText.text?.clear()
        clearIcon.visibility = View.GONE
        hideKeyboard()
    }

    private fun hideKeyboard() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(inputEditText.windowToken, 0)
    }

    @SuppressLint("MissingSuperCall")
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(SEARCH_QUERY_KEY, searchQuery)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        searchQuery = savedInstanceState.getString(SEARCH_QUERY_KEY, "")
        inputEditText.setText(searchQuery)
    }

    private fun handleWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(searchLayout) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun bottomNavigationView(){
        bottomNavigationView = findViewById(R.id.bottom_navigation)

        bottomNavigationView.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.navigation_search -> {true}
                R.id.navigation_media -> {
                    startActivityWithAnimation(MediaLibraryActivity::class.java)
                    true
                }
                R.id.navigation_settings -> {
                    startActivityWithAnimation(SettingsActivity::class.java)
                    true
                }
                else -> false
            }
        }
        bottomNavigationView.selectedItemId = R.id.navigation_search
    }

}