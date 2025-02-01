package com.example.playlistmaker

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.Switch
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class SettingsActivity : AppCompatActivity() {

    companion object {
        const val IS_BUTTON_ON_KEY = "isButtonOn"
        const val BUTTON_CLICKED_KEY = "button_clicked"
    }

    private var isButtonOn: Boolean = false
    private lateinit var backButton: TextView
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private lateinit var switchControl: Switch
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
        updateUI()
    }

    private fun setupState(savedInstanceState: Bundle?) {
        isButtonOn = savedInstanceState?.getBoolean(IS_BUTTON_ON_KEY, false)
            ?: intent.getBooleanExtra(IS_BUTTON_ON_KEY, false)
    }

    private fun initViews() {
        switchControl = findViewById(R.id.switch_control)
        backButton = findViewById(R.id.title)
        settingsLayout = findViewById(R.id.activity_settings)
    }

    private fun setupClickListeners() {
        switchControl.setOnCheckedChangeListener { _, isChecked ->
            isButtonOn = isChecked
            updateUI()
        }

        backButton.setOnClickListener { onBackButtonPressed() }

        val clickListener = View.OnClickListener { view ->
            when (view.id) {
                R.id.share -> shareApp()
                R.id.group -> writeToSupport()
                R.id.agreement -> openAgreement()
            }
        }

        listOf(R.id.share, R.id.group, R.id.agreement).forEach { id ->
            findViewById<TextView>(id).setOnClickListener(clickListener)
        }
    }

    private fun onBackButtonPressed() {
        setResult(RESULT_OK, Intent().apply { putExtra(IS_BUTTON_ON_KEY, isButtonOn) })
        finish()
    }

    private fun updateUI() {
        switchControl.isChecked = isButtonOn
        updateColors()
        updatePadding()
        updateSwitchColors()
    }

    private fun updateColors() {
        val textColorRes = if (isButtonOn) R.color.white else R.color.textColor
        val backgroundColorRes = if (isButtonOn) R.color.black else R.color.white

        settingsLayout.setBackgroundColor(ContextCompat.getColor(this, backgroundColorRes))
        setTextColorForAllTextViews(settingsLayout, textColorRes)

        val titleTextView = findViewById<TextView>(R.id.title)
        titleTextView.setTextColor(ContextCompat.getColor(this, textColorRes))
    }

    private fun setTextColorForAllTextViews(parentLayout: LinearLayout, colorResId: Int) {
        val color = ContextCompat.getColor(this, colorResId)
        for (i in 0 until parentLayout.childCount) {
            (parentLayout.getChildAt(i) as? TextView)?.setTextColor(color)
        }
    }

    private fun updatePadding() {
        backButton.setPadding(if (isButtonOn) (-25).dpToPx() else 0, backButton.paddingTop, backButton.paddingEnd, backButton.paddingBottom)
    }

    private fun handleWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(settingsLayout) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun updateSwitchColors() {
        val (thumbColor, trackColor) = if (isButtonOn) {
            Pair(R.color.switch_thumb_on_color, R.color.switch_track_on_color)
        } else {
            Pair(R.color.switch_thumb_off_color, R.color.switch_track_off_color)
        }

        switchControl.thumbTintList = ContextCompat.getColorStateList(this, thumbColor)
        switchControl.trackTintList = ContextCompat.getColorStateList(this, trackColor)
    }

    private fun setupHeader(buttonClicked: String?) {
        val titleTextView = findViewById<TextView>(R.id.title)
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
            data = Uri.parse("mailto:${getString(R.string.support_email)}")
            putExtra(Intent.EXTRA_SUBJECT, getString(R.string.support_subject))
            putExtra(Intent.EXTRA_TEXT, getString(R.string.support_body))
        }
        startActivity(Intent.createChooser(emailIntent, null))
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

    private fun Int.dpToPx(): Int {
        return (this * resources.displayMetrics.density).toInt()
    }
}