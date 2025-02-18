package com.example.playlistmaker

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.View
import android.widget.LinearLayout
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.appcompat.widget.Toolbar
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityOptionsCompat

class MainActivity : BaseActivity() {

    private lateinit var settingsLauncher: ActivityResultLauncher<Intent>
    private lateinit var mainLayout: LinearLayout
    private lateinit var toolbar: Toolbar

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        mainLayout = findViewById(R.id.activity_main)
        settingsLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val data: Intent? = result.data
                if (data != null) {
                    val isDarkTheme = data.getBooleanExtra("isDarkTheme", false)
                    setAppTheme(isDarkTheme)
                }
            }
        }

        ViewCompat.setOnApplyWindowInsetsListener(mainLayout) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        setupButtons()
    }

    private fun setAppTheme(isDarkTheme: Boolean) {
        AppCompatDelegate.setDefaultNightMode(
            if (isDarkTheme) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
        )
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }

    private fun setupButtons() {
        val buttonIds = listOf(R.id.Button_Big1, R.id.Button_Big2, R.id.Button_Big3)

        for (buttonId in buttonIds) {
            findViewById<View>(buttonId).setOnClickListener {
                when (buttonId) {
                    R.id.Button_Big1 -> launchActivityWithAnimation(SearchActivity::class.java, R.anim.fade_in, R.anim.fade_out)
                    R.id.Button_Big2 -> launchActivityWithAnimation(MediaLibraryActivity::class.java, R.anim.slide_in_right, R.anim.slide_out_left)
                    R.id.Button_Big3 -> launchActivityWithAnimation(SettingsActivity::class.java, R.anim.zoom_in, R.anim.zoom_out)
                }
            }
        }
    }

    private fun <T> launchActivityWithAnimation(activityClass: Class<T>, enterAnim: Int, exitAnim: Int) {
        val intent = Intent(this, activityClass)
        val options = ActivityOptionsCompat.makeCustomAnimation(this, enterAnim, exitAnim)
        startActivity(intent, options.toBundle())
    }

}