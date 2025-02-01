package com.example.playlistmaker

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.PorterDuff
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class SearchActivity : AppCompatActivity() {

    companion object {
        const val SEARCH_QUERY_KEY = "searchQuery"
    }

    private var isButtonOn = false
    private lateinit var backButton: TextView
    private lateinit var mainLayout: LinearLayout
    private lateinit var inputEditText: TextInputEditText
    private lateinit var clearIcon: ImageView
    private lateinit var titleTextView: TextView
    private lateinit var settingsLauncher: ActivityResultLauncher<Intent>
    private var searchQuery = ""

    @SuppressLint("CutPasteId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_search)

        initViews()
        retrieveIntentData()

        handleWindowInsets()

        updateBackgroundColor()
        setupActivityResultLauncher()
        setupListeners()
    }

    @SuppressLint("CutPasteId")
    private fun initViews() {
        backButton = findViewById(R.id.title)
        mainLayout = findViewById(R.id.activity_search)
        inputEditText = findViewById(R.id.inputEditText)
        clearIcon = findViewById(R.id.clearIcon)
        titleTextView = findViewById(R.id.title)
    }

    private fun retrieveIntentData() {
        isButtonOn = intent.getBooleanExtra(SettingsActivity.IS_BUTTON_ON_KEY, false)
    }

    private fun setupListeners() {
        backButton.setOnClickListener { onBackButtonPressed() }
        setupTextWatcher()
        setupClearButtonListener()
    }

    private fun setupTextWatcher() {
        inputEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                searchQuery = s.toString()
                clearIcon.visibility = if (!s.isNullOrEmpty()) View.VISIBLE else View.GONE
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun setupClearButtonListener() {
        clearIcon.setOnClickListener {
            inputEditText.text?.clear()
            clearIcon.visibility = View.GONE
            hideKeyboard()
        }
    }

    private fun hideKeyboard() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(inputEditText.windowToken, 0)
    }

    private fun onBackButtonPressed() {
        setResult(RESULT_OK, Intent().apply { putExtra(SettingsActivity.IS_BUTTON_ON_KEY, isButtonOn) })
        finish()
    }

    private fun setupActivityResultLauncher() {
        settingsLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                isButtonOn = result.data?.getBooleanExtra(SettingsActivity.IS_BUTTON_ON_KEY, false) ?: false
                Log.d("SearchActivity", "Switch state received: $isButtonOn")
                updateBackgroundColor()
            }
        }
    }

    private fun updateBackgroundColor() {
        val isDarkModeEnabled = isButtonOn
        mainLayout.setBackgroundColor(ContextCompat.getColor(this, if (isDarkModeEnabled) R.color.black else R.color.white))
        backButton.setTextColor(if (isDarkModeEnabled) ContextCompat.getColor(this, R.color.white) else ContextCompat.getColor(this, R.color.black))

        updateTitleIconColor(isDarkModeEnabled)
        updateSearchInputDrawableColor(isDarkModeEnabled)
        updateSearchBoxBackgroundColor(isDarkModeEnabled)
    }

    private fun updateTitleIconColor(isDarkModeEnabled: Boolean) {
        val arrowBackTintColor = if (isDarkModeEnabled) R.color.white else R.color.black
        ContextCompat.getDrawable(this, R.drawable.arrow_back)?.mutate()?.let { drawable ->
            drawable.setColorFilter(ContextCompat.getColor(this, arrowBackTintColor), PorterDuff.Mode.SRC_IN)
            titleTextView.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null)
        }
    }

    private fun updateSearchInputDrawableColor(isDarkModeEnabled: Boolean) {
        val drawableTint = if (isDarkModeEnabled) R.color.textColor else R.color.hintSearchIcon
        ContextCompat.getDrawable(this, R.drawable.magnifier)?.apply {
            setColorFilter(ContextCompat.getColor(this@SearchActivity, drawableTint), PorterDuff.Mode.SRC_IN)
            inputEditText.setCompoundDrawablesRelativeWithIntrinsicBounds(this, null, null, null)
        }
    }

    private fun updateSearchBoxBackgroundColor(isDarkModeEnabled: Boolean) {
        val boxBackgroundColor = if (isDarkModeEnabled) R.color.white else R.color.hintFieldColor
        val searchInputLayout = findViewById<TextInputLayout>(R.id.search_box)
        searchInputLayout.setBoxBackgroundColor(ContextCompat.getColor(this, boxBackgroundColor))
    }

    private fun handleWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(mainLayout) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(SEARCH_QUERY_KEY, searchQuery)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        searchQuery = savedInstanceState.getString(SEARCH_QUERY_KEY, "")
        inputEditText.setText(searchQuery)
    }
}