package com.example.playlistmaker

import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityOptionsCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.bottomnavigation.BottomNavigationView

class MediaLibraryActivity : BaseActivity() {

    private lateinit var backButton: ImageView
    private lateinit var toolbar: Toolbar
    private lateinit var mediaLayout: LinearLayout
    private lateinit var bottomNavigationView: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        enableEdgeToEdge()
        setContentView(R.layout.activity_media_library)
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        initViews()
        handleWindowInsets()
        setupClickListeners()
        bottomNavigationView()
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

    private fun initViews(){
        backButton = findViewById(R.id.backArrow)
        mediaLayout = findViewById(R.id.activity_media_library)
    }

    private fun handleWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(mediaLayout) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun setupClickListeners(){
        backButton.setOnClickListener {
            ActivityOptionsCompat.makeCustomAnimation(
                this, R.anim.enter_from_left, R.anim.exit_to_right
            ).toBundle()

            finishAfterTransition()
        }
    }

    private fun bottomNavigationView(){
        bottomNavigationView = findViewById(R.id.bottom_navigation)

        bottomNavigationView.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.navigation_search -> {
                    startActivityWithAnimation(SearchActivity::class.java)
                    true
                }
                R.id.navigation_media -> {
                    true
                }
                R.id.navigation_settings -> {
                    startActivityWithAnimation(SettingsActivity::class.java)
                    true
                }
                else -> false
            }
        }
        bottomNavigationView.selectedItemId = R.id.navigation_media
    }
}