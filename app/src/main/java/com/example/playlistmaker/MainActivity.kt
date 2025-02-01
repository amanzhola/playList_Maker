package com.example.playlistmaker

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {

    private var isButtonOn = false
    private lateinit var mainLayout: LinearLayout
    private lateinit var settingsLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mainLayout = findViewById(R.id.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(mainLayout) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        isButtonOn = savedInstanceState?.getBoolean(SettingsActivity.IS_BUTTON_ON_KEY, false) ?: isButtonOn
        start()
    }

    private fun start() {
        setupActivityResultLauncher()
        setupButtons()
        updateBackgroundColor()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(SettingsActivity.IS_BUTTON_ON_KEY, isButtonOn)
    }

    private fun setupActivityResultLauncher() {
        settingsLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                isButtonOn = result.data?.getBooleanExtra(SettingsActivity.IS_BUTTON_ON_KEY, false) ?: false
                updateBackgroundColor()
            }
        }
    }

    private fun setupButtons() {

        val buttonIds = listOf(R.id.Button_Big1, R.id.Button_Big2, R.id.Button_Big3)

        for (buttonId in buttonIds) {
            findViewById<View>(buttonId).setOnClickListener {
                when (buttonId) {
                    R.id.Button_Big1 -> launchActivity(SearchActivity::class.java)
                    R.id.Button_Big2 -> launchActivity(SettingsActivity::class.java, ScreenType.MEDIA.name)
                    R.id.Button_Big3 -> launchActivity(SettingsActivity::class.java)
                }
            }
        }
    }
    private fun <T> launchActivity(activityClass: Class<T>, extra: String? = null) {
        Intent(this, activityClass).apply {
            extra?.let { putExtra(SettingsActivity.BUTTON_CLICKED_KEY, it) }
            putExtra(SettingsActivity.IS_BUTTON_ON_KEY, isButtonOn)
            // Запускаем активность с помощью лончера
            settingsLauncher.launch(this)
        }
    }

    private fun updateBackgroundColor() {
        val backgroundColorRes = if (isButtonOn) R.color.black else R.color.backgroundDay
        mainLayout.setBackgroundColor(ContextCompat.getColor(this, backgroundColorRes))
    }
}

enum class ScreenType {
    MEDIA
}