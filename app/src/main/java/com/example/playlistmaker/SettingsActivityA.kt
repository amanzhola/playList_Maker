package com.example.playlistmaker

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.core.content.ContextCompat
import com.google.android.material.switchmaterial.SwitchMaterial

class SettingsActivityA : BaseActivity() {

    companion object {
        const val IS_BUTTON_ON_KEY = "isButtonOn"
        const val BUTTON_CLICKED_KEY = "button_clicked"
    }

    private lateinit var switchControl: SwitchMaterial
    private lateinit var settingsLayout: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_settings)

        initViews()
        setupState(savedInstanceState)
        setupClickListeners()
        handleWindowInsets(settingsLayout)
        setupHeader(intent.getStringExtra(BUTTON_CLICKED_KEY))
        updateUI()
    }

    private fun setupState(savedInstanceState: Bundle?) {
        isButtonOn = savedInstanceState?.getBoolean(IS_BUTTON_ON_KEY, false)
            ?: intent.getBooleanExtra(IS_BUTTON_ON_KEY, false)
    }

    private fun initViews() {
        switchControl = find(R.id.switch_control)
        settingsLayout = find(R.id.activity_settings)
    }

    private fun setupClickListeners() {
        switchControl.setOnCheckedChangeListener { _, isChecked ->
            isButtonOn = isChecked
            updateUI()
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

    private fun updateUI() {
        switchControl.isChecked = isButtonOn
        updateColors()
        updateBackArrowVisibility()
        updateSwitchColors()
    }

    private fun updateColors() {
        val backgroundColors = getColorPair(R.color.textColor, R.color.white )
        updateColors(settingsLayout, null, backgroundColors)

        val textColorRes = if (isButtonOn) R.color.white else R.color.textColor

        title.setTextColor(ContextCompat.getColor(this, textColorRes))
        setTextColorForAllTextViews(settingsLayout, textColorRes)
    }

    private fun setTextColorForAllTextViews(parentLayout: LinearLayout, colorResId: Int) {
        val color = ContextCompat.getColor(this, colorResId)
        for (i in 0 until parentLayout.childCount) {
            (parentLayout.getChildAt(i) as? TextView)?.setTextColor(color)
        }
    }

    private fun updateBackArrowVisibility() {
        val layoutParams = backButton.layoutParams as ViewGroup.MarginLayoutParams
        layoutParams.width = if (isButtonOn) 0 else resources.getDimensionPixelSize(R.dimen.arrow_back_24)
        layoutParams.marginEnd = if (isButtonOn) resources.getDimensionPixelSize(R.dimen.backArrowNight) else resources.getDimensionPixelSize(R.dimen.arrow_back_padding_12)
        backButton.layoutParams = layoutParams
        backButton.requestLayout()
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
        val titleTextView = find<TextView>(R.id.title)
        titleTextView.text = when (buttonClicked) {
            ScreenTypeA.MEDIA.name -> {
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

        // Проверка на наличие почтового клиента
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

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(IS_BUTTON_ON_KEY, isButtonOn)
    }
}