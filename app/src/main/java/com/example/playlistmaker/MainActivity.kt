package com.example.playlistmaker

import android.os.Bundle
import android.view.View
import android.widget.LinearLayout

class MainActivity : BaseActivity() {

    private lateinit var mainLayout: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mainLayout = findViewById(R.id.activity_main)

        handleWindowInsets(mainLayout)
        isButtonOn = savedInstanceState?.getBoolean(SettingsActivity.IS_BUTTON_ON_KEY, false) ?: false

        initialize()
    }

    private fun initialize() {
        setupActivityResultLauncher { isButtonOnResult ->
            isButtonOn = isButtonOnResult
            updateBackgroundColor()
        }
        setupButtons()
        updateBackgroundColor()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(SettingsActivity.IS_BUTTON_ON_KEY, isButtonOn)
    }

    private fun setupButtons() {
        val buttonIds = listOf(R.id.Button_Big1, R.id.Button_Big2, R.id.Button_Big3)

        for (buttonId in buttonIds) {
            findViewById<View>(buttonId).setOnClickListener {
                when (buttonId) {
                    R.id.Button_Big1 -> launchActivity(SearchActivity::class.java, isButtonOn)
                    R.id.Button_Big2 -> launchActivity(SettingsActivity::class.java, isButtonOn, ScreenType.MEDIA.name)
                    R.id.Button_Big3 -> launchActivity(SettingsActivity::class.java, isButtonOn)
                }
            }
        }
    }

    private fun updateBackgroundColor() {
        val backgroundColor = getColor(R.color.black, R.color.backgroundDay)
        val textColor =  getColor(R.color.white, R.color.black)
        updateColors(mainLayout, null, Pair(backgroundColor, textColor))
    }
}

enum class ScreenType {
    MEDIA
}