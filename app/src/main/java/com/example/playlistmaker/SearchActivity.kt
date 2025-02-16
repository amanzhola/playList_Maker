package com.example.playlistmaker

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class SearchActivity : AppCompatActivity() {

    companion object {
        const val SEARCH_QUERY_KEY = "searchQuery"
    }

    private var isButtonOn = false
    private lateinit var backButton: ImageView
    private lateinit var titleText: TextView
    private lateinit var mainLayout: LinearLayout
    private lateinit var inputEditText: TextInputEditText
    private lateinit var clearIcon: ImageView
    private lateinit var searchInputLayout: TextInputLayout
    private lateinit var settingsLauncher: ActivityResultLauncher<Intent>
    private var searchQuery = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_search)

        initViews()
        retrieveIntentData()
        handleWindowInsets()
        setupActivityResultLauncher()
        setupListeners()
    }

    private fun <T : View> findView(id: Int): T {
        return findViewById(id) as T
    }

    private fun initViews() {
        backButton = findView(R.id.backArrow)
        titleText = findView(R.id.title)
        mainLayout = findView(R.id.activity_search)
        inputEditText = findView(R.id.inputEditText)
        clearIcon = findView(R.id.clearIcon)
        searchInputLayout = findView(R.id.search_box)
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

    private fun setupActivityResultLauncher() {
        settingsLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                isButtonOn = result.data?.getBooleanExtra(SettingsActivity.IS_BUTTON_ON_KEY, false) ?: false
                if (isButtonOn) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
            }
        }
    }

    private fun handleWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(mainLayout) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    @SuppressLint("MissingSuperCall")
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(SEARCH_QUERY_KEY, searchQuery)
        outState.putBoolean("isButtonOn", isButtonOn)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        searchQuery = savedInstanceState.getString(SEARCH_QUERY_KEY, "")
        isButtonOn = savedInstanceState.getBoolean("isButtonOn", false)
        inputEditText.setText(searchQuery)
    }
}