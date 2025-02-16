package com.example.playlistmaker

import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.Menu
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityOptionsCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.switchmaterial.SwitchMaterial

open class SettingsActivity : BaseActivity(), SettingsChangeListener {

    private lateinit var backButton: ImageView
    private lateinit var switchControl: SwitchMaterial
    private lateinit var settingsLayout: LinearLayout
    private lateinit var sharedPreferences: SharedPreferences
    private var isDarkTheme: Boolean = false
    private lateinit var toolbar: Toolbar
    private lateinit var bottomNavigationView: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sharedPreferences = getSharedPreferences("AppSettings", Context.MODE_PRIVATE)
        isDarkTheme = sharedPreferences.getBoolean("isDarkTheme", false)

        setTheme(isDarkTheme)

        setContentView(R.layout.activity_settings)
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        initViews()
        handleWindowInsets()
        setupClickListeners()

        bottomNavigationView = findViewById(R.id.bottom_navigation)

        bottomNavigationView.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.navigation_search -> {
                    startActivityWithAnimation(SearchActivity::class.java)
                    true
                }

                R.id.navigation_media -> {
                    startActivityWithAnimation(MediaLibraryActivity::class.java)
                    true
                }

                R.id.navigation_settings -> {
                    true
                }

                else -> false
            }
        }
        bottomNavigationView.selectedItemId = R.id.navigation_settings

    }

    private fun startActivityWithAnimation(targetActivity: Class<*>) {
        val intent = Intent(this, targetActivity)
        val options = ActivityOptions.makeCustomAnimation(
            this,
                R.anim.enter_from_bottom,
            R.anim.exit_to_top
        )
        startActivity(intent, options.toBundle())
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        val dropdownItem = menu.findItem(R.id.dropdown)
        dropdownItem?.isVisible = false
        return true
    }

    private fun <T : View> find(id: Int): T { return findViewById(id)!!
    }

    private fun initViews() {
        switchControl = findViewById(R.id.switch_control)
        backButton = findViewById(R.id.backArrow)
        settingsLayout = findViewById(R.id.activity_settings)

        switchControl.isChecked = isDarkTheme
    }

    private fun setupClickListeners() {
        switchControl.setOnCheckedChangeListener { _, isChecked ->
            onThemeChanged(isChecked)
        }
        setupViewClickListener<ImageView>(R.id.backArrow) {
            if (!isDarkTheme) {
                ActivityOptionsCompat.makeCustomAnimation(
                    this,
                    R.anim.enter_from_left,
                    R.anim.exit_to_right
                ).toBundle()
                ActivityCompat.finishAfterTransition(this)
            }
        }
        setupViewClickListener<TextView>(R.id.share) { shareApp() }
        setupViewClickListener<TextView>(R.id.group) { writeToSupport() }
        setupViewClickListener<TextView>(R.id.agreement) { openAgreement() }
        setupViewClickListener<TextView>(R.id.title) {
            if (isDarkTheme) {
                ActivityOptionsCompat.makeCustomAnimation(
                    this,
                    R.anim.fade_in,
                    R.anim.fade_out
                ).toBundle()
                ActivityCompat.finishAfterTransition(this)
            }
        }
    }


    private fun <T : View> setupViewClickListener(viewId: Int, action: () -> Unit) {
        find<T>(viewId).setOnClickListener { action() }
    }

    private fun handleWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(settingsLayout) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    override fun onThemeChanged(isDarkTheme: Boolean) {
        val editor = sharedPreferences.edit()
        editor.putBoolean("isDarkTheme", isDarkTheme)
        editor.apply()

        setTheme(isDarkTheme)

        switchControl.isChecked = isDarkTheme
    }

    private fun setTheme(isDarkTheme: Boolean) {
        AppCompatDelegate.setDefaultNightMode(
            if (isDarkTheme) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
        )
    }
}