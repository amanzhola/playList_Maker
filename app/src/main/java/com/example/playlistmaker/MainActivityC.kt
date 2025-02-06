package com.example.playlistmaker

import android.content.Intent
import android.os.Bundle
import android.view.View

class MainActivityC : BaseActivityC() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main1)

        setupToolbar(R.id.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(false)
        setupButtons()
    }

    private fun setupButtons() {
        val buttonMappings = mapOf(
            R.id.Button_Big1 to SearchActivityC::class.java,
            R.id.Button_Big2 to MediaLibraryActivityC::class.java,
            R.id.Button_Big3 to SettingsActivityC::class.java
        )

        buttonMappings.forEach { (buttonId, activityClass) ->
            findViewById<View>(buttonId).setOnClickListener {
                startActivity(Intent(this, activityClass))
            }
        }
    }
}