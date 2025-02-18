package com.example.playlistmaker

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate

open class BaseActivity : AppCompatActivity(), CircleSegmentsView.OnSegmentClickListener {

    private lateinit var sharedPreferences: android.content.SharedPreferences
    private var isDarkTheme: Boolean = false

    override fun onCreate(savedInstanceState: android.os.Bundle?) {
        super.onCreate(savedInstanceState)
        sharedPreferences = getSharedPreferences("AppSettings", Context.MODE_PRIVATE)
        isDarkTheme = sharedPreferences.getBoolean("isDarkTheme", false)
        setTheme(isDarkTheme)

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.dropdown -> {
                val dialog = CircleSegmentsDialog(this, this)
                dialog.show()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSegmentClicked(segmentIndex: Int) {
        when (segmentIndex) {
            0 -> {
                isDarkTheme = !isDarkTheme
                val editor = sharedPreferences.edit()
                editor.putBoolean("isDarkTheme", isDarkTheme)
                editor.apply()

                setTheme(isDarkTheme)
            }
            1 -> { shareApp() }
            2 -> { writeToSupport() }
            3 -> { openAgreement() }
            4 -> { onSegment4Clicked() }
        }
    }

    open fun onSegment4Clicked() { }

    private fun setTheme(isDarkTheme: Boolean) {
        AppCompatDelegate.setDefaultNightMode(
            if (isDarkTheme) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
        )
    }

    protected fun shareApp() {
        val shareMessage = getString(R.string.share_message)
        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, shareMessage)
            type = "text/plain"
        }
        startActivity(Intent.createChooser(sendIntent, null))
    }

    protected fun writeToSupport() {
        val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:${getString(R.string.support_email)}?subject=${getString(R.string.support_subject)}&body=${getString(R.string.support_body)}")
        }
        startActivity(Intent.createChooser(emailIntent, null))

        if (emailIntent.resolveActivity(packageManager) != null) {
            startActivity(emailIntent)
        } else {
            Toast.makeText(this, "Нет приложения для отправки электронной почты", Toast.LENGTH_SHORT).show()
        }
    }

    protected fun openAgreement() {
        val agreementUrl = getString(R.string.agreement_url)
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(agreementUrl))
        startActivity(browserIntent)
    }

}