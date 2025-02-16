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
import androidx.core.app.ActivityOptionsCompat

open class MainActivity : BaseActivity() {

    private lateinit var settingsLauncher: ActivityResultLauncher<Intent>
    private lateinit var mainLayout: LinearLayout
    private lateinit var toolbar: Toolbar

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        mainLayout = findViewById(R.id.activity_main)
        settingsLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {}

        ViewCompat.setOnApplyWindowInsetsListener(mainLayout) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupButtons()

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
                    R.id.Button_Big1 -> launchActivity(SearchActivity::class.java)
                    R.id.Button_Big2 -> launchActivity(MediaLibraryActivity::class.java)
                    R.id.Button_Big3 -> launchActivity(SettingsActivity::class.java)
                }
            }
        }
    }

    private fun <T> launchActivity(activityClass: Class<T>, enterAnim: Int = 0, exitAnim: Int = 0) {
        val intent = Intent(this, activityClass)

        if (enterAnim != 0 || exitAnim != 0) {
            val options = ActivityOptionsCompat.makeCustomAnimation(
                this, enterAnim, exitAnim
            )
            settingsLauncher.launch(intent, options)
        } else {
            settingsLauncher.launch(intent)
        }
    }
}
