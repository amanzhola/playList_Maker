package com.example.playlistmaker

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.switchmaterial.SwitchMaterial

class SettingsActivity : AppCompatActivity() {

    companion object {
        const val IS_BUTTON_ON_KEY = "isButtonOn"
        const val BUTTON_CLICKED_KEY = "button_clicked"
    }

    private var isButtonOn: Boolean = false
    private lateinit var backButton: ImageView
    private lateinit var switchControl: SwitchMaterial
    private lateinit var settingsLayout: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_settings)

        initViews()
        setupState(savedInstanceState)
        setupClickListeners()
        handleWindowInsets()
        setupHeader(intent.getStringExtra(BUTTON_CLICKED_KEY))
        switchControl.isChecked = isButtonOn
    }

    private fun setupState(savedInstanceState: Bundle?) {
        isButtonOn = savedInstanceState?.getBoolean(IS_BUTTON_ON_KEY, false)
            ?: intent.getBooleanExtra(IS_BUTTON_ON_KEY, false)
    }

    private fun <T : View> find(id: Int): T {
        return findViewById(id) as T
    }

    private fun initViews() {
        switchControl = find(R.id.switch_control)
        backButton = find(R.id.backArrow)
        settingsLayout = find(R.id.activity_settings)
    }

    private fun setupClickListeners() {
        switchControl.setOnCheckedChangeListener { _, isChecked ->
            isButtonOn = isChecked
            AppCompatDelegate.setDefaultNightMode(
                if (isButtonOn) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO)
        }

        setupViewClickListener<ImageView>(R.id.backArrow) { if (!isButtonOn) onBackButtonPressed() }
        setupViewClickListener<TextView>(R.id.share) { shareApp() }
        setupViewClickListener<TextView>(R.id.group) { writeToSupport() }
        setupViewClickListener<TextView>(R.id.agreement) { openAgreement() }
        setupViewClickListener<TextView>(R.id.title) { if (isButtonOn) onBackButtonPressed() }
    }

    private fun <T : View> setupViewClickListener(viewId: Int, action: () -> Unit) {
        find<T>(viewId).setOnClickListener { action() }
    }

    private fun onBackButtonPressed() {
        setResult(RESULT_OK, Intent().apply { putExtra(IS_BUTTON_ON_KEY, isButtonOn) })
        finish()
    }

    private fun handleWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(settingsLayout) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun setupHeader(buttonClicked: String?) {
        val titleTextView = find<TextView>(R.id.title)
        titleTextView.text = when (buttonClicked) {
            ScreenType.MEDIA.name -> {
                hideOtherViews()
                getString(R.string.media)
            }
            else -> getString(R.string.list0)
        }
    }

    private fun hideOtherViews() {
        for (i in 0 until settingsLayout.childCount) {
            val child = settingsLayout.getChildAt(i)
            if (child is TextView) {
                child.visibility = View.GONE
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

    private fun writeToSupport() {
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

    private fun openAgreement() {
        val agreementUrl = getString(R.string.agreement_url)
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(agreementUrl))
        startActivity(browserIntent)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(IS_BUTTON_ON_KEY, isButtonOn)
    }
}