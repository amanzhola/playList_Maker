package com.example.playlistmaker

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.activity.enableEdgeToEdge
import androidx.core.content.ContextCompat
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class SearchActivity : BaseActivity() {

    companion object {
        const val SEARCH_QUERY_KEY = "searchQuery"
    }

    private lateinit var mainLayout: LinearLayout
    private lateinit var inputEditText: TextInputEditText
    private lateinit var clearIcon: ImageView
    private lateinit var searchInputLayout: TextInputLayout
    private var searchQuery = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_search)

        initViews()
        retrieveIntentData()
        handleWindowInsets(mainLayout)
        setupListeners()
        updateUI()
    }

    private fun initViews() {
        mainLayout = find(R.id.activity_search)
        inputEditText = find(R.id.inputEditText)
        clearIcon = find(R.id.clearIcon)
        searchInputLayout = find(R.id.search_box)
    }

    private fun retrieveIntentData() {
        isButtonOn = intent.getBooleanExtra(SettingsActivity.IS_BUTTON_ON_KEY, false)
    }

    private fun setupListeners() {
        backButton.setOnClickListener { onBackButtonPressed() }
        inputEditText.addTextChangedListener(createTextWatcher())
        clearIcon.setOnClickListener { clearSearchInput() }
    }

    private fun createTextWatcher(): TextWatcher {
        return object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                searchQuery = s.toString()
                clearIcon.visibility = if (!s.isNullOrEmpty()) View.VISIBLE else View.GONE
            }

            override fun afterTextChanged(s: Editable?) {}
        }
    }

    private fun clearSearchInput() {
        inputEditText.text?.clear()
        clearIcon.visibility = View.GONE
        hideKeyboard()
    }

    private fun hideKeyboard() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(inputEditText.windowToken, 0)
    }

    private fun onBackButtonPressed() {
        setResult(RESULT_OK, Intent().apply { putExtra(SettingsActivity.IS_BUTTON_ON_KEY, isButtonOn) })
        finish()
    }

    private fun updateUI() {
        updateBackgroundColor()
        updateIconAndInputColors()
    }

    private fun updateBackgroundColor() {
        val backgroundColor = getColor(R.color.textColor, R.color.white)
        val titleColor = getColor(R.color.white, R.color.textColor)

        mainLayout.setBackgroundColor(ContextCompat.getColor(this, backgroundColor))
        title.setTextColor(ContextCompat.getColor(this, titleColor))
    }

    private fun updateIconAndInputColors() {
        val iconColor = getColor(R.color.white, R.color.textColor)
        backButton.setImageDrawable(getColoredDrawable(R.drawable.arrow_back, iconColor))

        val drawableTint = getColor(R.color.textColor, R.color.hintSearchIcon)
        inputEditText.setCompoundDrawablesRelativeWithIntrinsicBounds(getColoredDrawable(R.drawable.magnifier, drawableTint), null, null, null)

        val boxBackgroundColor = getColor(R.color.white,R.color.hintFieldColor)
        searchInputLayout.setBoxBackgroundColor(ContextCompat.getColor(this, boxBackgroundColor))
    }

    private fun getColoredDrawable(drawableId: Int, colorId: Int): Drawable? {
        val drawable = ContextCompat.getDrawable(this, drawableId)?.mutate() ?: return null
        val colorFilter = PorterDuffColorFilter(
            ContextCompat.getColor(this@SearchActivity, colorId),
            PorterDuff.Mode.SRC_IN
        )
        drawable.colorFilter = colorFilter
        return drawable
    }

    @SuppressLint("MissingSuperCall")
    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString(SEARCH_QUERY_KEY, searchQuery)
        outState.putBoolean(SettingsActivity.IS_BUTTON_ON_KEY, isButtonOn)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        searchQuery = savedInstanceState.getString(SEARCH_QUERY_KEY, "")
        isButtonOn = savedInstanceState.getBoolean(SettingsActivity.IS_BUTTON_ON_KEY, false)
        inputEditText.setText(searchQuery)
        updateUI() // Обновление фонового цвета и остальных элементов при восстановлении состояния
    }
}