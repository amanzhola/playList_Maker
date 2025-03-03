package com.example.playlistmaker

import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.TextView
import androidx.core.app.ActivityOptionsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class SearchActivity : BaseActivity() {

    companion object {
        const val SEARCH_QUERY_KEY = "searchQuery"
        const val TRACK_LIST_KEY = "trackList" }

    private lateinit var backButton: ImageView
    private lateinit var inputEditText: TextInputEditText
    private lateinit var clearIcon: ImageView
    private lateinit var searchInputLayout: TextInputLayout
    private var searchQuery = ""
    private var isBottomNavVisible = true
    private lateinit var adapter: TrackAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var apiService: ITunesApi
    private var trackList: List<Track> = emptyList()
    private lateinit var textView: TextView
    private lateinit var update: MaterialButton
    private var lastSearchQuery: String? = null
    private var isErrorVisible = false
    private var isErrorTypeNoResults: Boolean = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        enableEdgeToEdge()

        initViews()
        setupListeners()
        setupBottomNavigationView()
        bottomNavigationView.selectedItemId = R.id.navigation_search

        apiService = Retrofit.Builder()
            .baseUrl("https://itunes.apple.com")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ITunesApi::class.java)

        recyclerView = findViewById(R.id.tracks_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this)

        if (savedInstanceState != null) {
            searchQuery = savedInstanceState.getString(SEARCH_QUERY_KEY, "")

            val jsonTrackList = savedInstanceState.getString(TRACK_LIST_KEY, "")
            if (jsonTrackList.isNotEmpty()) {
                trackList = Gson().fromJson(jsonTrackList, Array<Track>::class.java).toList()
            }
        }

        adapter = TrackAdapter(trackList, this)
        recyclerView.adapter = adapter
    }

    override fun onSegment4Clicked() {
        if (isBottomNavVisible) {
            bottomNavigationView.animate().translationY(bottomNavigationView.height.toFloat()).alpha(0f).setDuration(300).withEndAction {
                bottomNavigationView.visibility = View.GONE
            }
            line.animate().translationY(line.height.toFloat()).alpha(0f).setDuration(300).withEndAction {
                line.visibility = View.GONE            }
        } else {
            bottomNavigationView.visibility = View.VISIBLE
            line.visibility = View.VISIBLE

            bottomNavigationView.alpha = 0f
            bottomNavigationView.translationY = bottomNavigationView.height.toFloat()
            bottomNavigationView.animate().translationY(0f).alpha(1f).setDuration(300)

            line.alpha = 0f
            line.translationY = line.height.toFloat()
            line.animate().translationY(0f).alpha(1f).setDuration(300)
        }
        isBottomNavVisible = !isBottomNavVisible
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_search
    }

    override fun getMainLayoutId(): Int {
        return R.id.activity_search
    }

    private fun initViews() {
        backButton = findViewById(R.id.backArrow)
        inputEditText = findViewById(R.id.inputEditText)
        clearIcon = findViewById(R.id.clearIcon)
        searchInputLayout = findViewById(R.id.search_box)
        textView = findViewById(R.id.fail)
        update = findViewById(R.id.btnUpdate)
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
            if (hasFocus) {
                bottomNavigationView.visibility = View.GONE
                line.visibility = View.GONE
            } else {
                bottomNavigationView.visibility = View.VISIBLE
                line.visibility = View.VISIBLE
            }
        }

        inputEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                performSearch()
                true
            } else {
                false
            }
        }
    }

    private fun createTextWatcher(): TextWatcher {
        return object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) { }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                searchQuery = s.toString()
                val inputWidth: Int = inputEditText.width
                val characterWidth: Float = inputEditText.paint.measureText("�")
                var maxCharacterCount: Int = calculateMaxCharacterCount(inputWidth, characterWidth, 0.75)

                if (!s.isNullOrEmpty()) {
                    maxCharacterCount = when {
                        s.all { it.isLowerCase() && it in '�'..'�' } ->
                            calculateMaxCharacterCount(inputWidth, characterWidth, 0.55)
                        s.all { it.isUpperCase() && it in '�'..'�' } ->
                            calculateMaxCharacterCount(inputWidth, characterWidth, 0.50)
                        s.all { it.isLowerCase() && it in 'a'..'z' } ->
                            calculateMaxCharacterCount(inputWidth, characterWidth, 0.60)
                        s.all { it.isUpperCase() && it in 'A'..'Z' } ->
                            calculateMaxCharacterCount(inputWidth, characterWidth, 0.55)
                        else -> maxCharacterCount
                    }
                }
                if ((s != null) && (s.length > maxCharacterCount)) {
                    inputEditText.setText(s.substring(0, maxCharacterCount))
                    inputEditText.setSelection(maxCharacterCount)
                }
                clearIcon.visibility = if (!s.isNullOrEmpty()) View.VISIBLE else View.GONE
            }
            override fun afterTextChanged(s: Editable?) { }
        }
    }

    private fun calculateMaxCharacterCount(maxWidth: Int, charWidth: Float, percentage: Double): Int {
        return (maxWidth * percentage / charWidth).toInt()
    }

    private fun clearSearchInput() {
        inputEditText.text?.clear()
        clearIcon.visibility = View.GONE
        hideKeyboard()

        trackList = emptyList()
        adapter.updateTracks(trackList)
    }

    private fun hideKeyboard() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(inputEditText.windowToken, 0)
    }

    @SuppressLint("MissingSuperCall")
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(SEARCH_QUERY_KEY, searchQuery)
        outState.putString(TRACK_LIST_KEY, Gson().toJson(trackList))
        outState.putBoolean("isErrorVisible", isErrorVisible)
        outState.putBoolean("isErrorTypeNoResults", isErrorTypeNoResults)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        searchQuery = savedInstanceState.getString(SEARCH_QUERY_KEY, "")
        inputEditText.setText(searchQuery)

        val jsonTrackList = savedInstanceState.getString(TRACK_LIST_KEY, "")
        if (jsonTrackList.isNotEmpty()) {
            trackList = Gson().fromJson(jsonTrackList, Array<Track>::class.java).toList()
            adapter = TrackAdapter(trackList, this)
            recyclerView.adapter = adapter
        }
        isErrorVisible = savedInstanceState.getBoolean("isErrorVisible", false)
        isErrorTypeNoResults = savedInstanceState.getBoolean("isErrorTypeNoResults", false)
        if (isErrorVisible){
            showErrorPlaceholder(isErrorTypeNoResults)
        }
    }

    private fun showErrorPlaceholder(isNoResults: Boolean) {
        isErrorVisible = true
        isErrorTypeNoResults = isNoResults

        textView.visibility = View.VISIBLE

        if (isNoResults) {
            textView.isEnabled = true
            textView.setText(R.string.searchFail)
            update.visibility = View.GONE
        } else {
            textView.isEnabled = false
            textView.setText(R.string.networkFail)
            update.visibility = View.VISIBLE

            update.setOnClickListener {
                lastSearchQuery?.let { query ->
                    inputEditText.setText(query)
                    performSearch()
                }
            }
        }
    }

    private fun performSearch() {
        val query = inputEditText.text.toString().trim()
        if (query.isNotEmpty()) {
            lastSearchQuery = query

            if (!isNetworkAvailable(this)) {
                clearTracksAndShowError(false)
                showErrorPlaceholder(false)
                return
            }

            apiService.search(query).enqueue(object : Callback<SearchResponse> {
                override fun onResponse(call: Call<SearchResponse>, response: Response<SearchResponse>) {
                    if (response.isSuccessful && response.body() != null) {
                        trackList = response.body()!!.results
                        updateRecyclerView()
                    } else {
                        clearTracksAndShowError(trackList.isEmpty())
                    }
                }

                override fun onFailure(call: Call<SearchResponse>, t: Throwable) {
                    clearTracksAndShowError(false)
                }
            })
        }
    }

    private fun updateRecyclerView() {
        if (trackList.isEmpty()) {
            clearTracksAndShowError(true)
        } else {
            isErrorVisible = false
            textView.visibility = View.GONE
            update.visibility = View.GONE

            adapter.updateTracks(trackList)
        }
    }
    private fun clearTracksAndShowError(isNoResults: Boolean) {
        trackList = emptyList()
        adapter.updateTracks(trackList)
        showErrorPlaceholder(isNoResults)
    }
    
    fun getAdapter(): TrackAdapter {
        return adapter
    }

    fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkCapabilities = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
        return networkCapabilities != null && (networkCapabilities.hasTransport(
            NetworkCapabilities.TRANSPORT_WIFI) || networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR))
    }
}