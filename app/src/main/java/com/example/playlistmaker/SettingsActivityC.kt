package com.example.playlistmaker

import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.Toolbar
import com.google.android.material.switchmaterial.SwitchMaterial

class SettingsActivityC : BaseActivityC() {

    lateinit var toolbar: Toolbar
    private lateinit var switchControl: SwitchMaterial
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings1)

        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        toolbar.setOnClickListener {
            finish()
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        sharedPreferences = getSharedPreferences("AppSettings", MODE_PRIVATE)
        switchControl = findViewById(R.id.switch_control)

        switchControl.setOnCheckedChangeListener { _, isChecked ->
            toggleNightMode(isChecked)
            toggleBackButton()
        }
        applyNightMode()

        listOf(R.id.share, R.id.group, R.id.agreement).forEach { viewId ->
            findViewById<View>(viewId).setOnClickListener {
                when (viewId) {
                    R.id.share -> shareApp()
                    R.id.group -> writeToSupport()
                    R.id.agreement -> openAgreement()
                }
            }
        }
    }

    private fun shareApp() {
        val shareMessage = getString(R.string.share_message)
        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, shareMessage)
            type = "text/plain"
        }
        startActivity(Intent.createChooser(sendIntent, null))
    }

    @SuppressLint("QueryPermissionsNeeded")
    private fun writeToSupport() {
        val email = getString(R.string.support_email)
        val subject = getString(R.string.support_subject)
        val body = getString(R.string.support_body)

        val emailIntent = Intent(Intent.ACTION_SEND).apply {
            type = "message/rfc822"
            putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, body)
        }

        if (emailIntent.resolveActivity(packageManager) != null) {
            startActivity(Intent.createChooser(emailIntent, null))
        } else {
            Toast.makeText(this, "Нет доступного почтового клиента", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openAgreement() {
        val agreementUrl = getString(R.string.agreement_url)
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(agreementUrl))
        startActivity(browserIntent)
    }

    private fun applyNightMode() {
        val isDarkMode = sharedPreferences.getBoolean("DarkMode", false)
        toggleNightMode(isDarkMode)
        toggleBackButton()
    }

    private fun toggleNightMode(isChecked: Boolean) {
        if (isChecked) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
        with(sharedPreferences.edit()) {
            putBoolean("DarkMode", isChecked)
            apply()
        }
        switchControl.isChecked = isChecked
    }

    private fun toggleBackButton() {
        supportActionBar?.setDisplayHomeAsUpEnabled(!sharedPreferences.getBoolean("DarkMode", false))
    }

}