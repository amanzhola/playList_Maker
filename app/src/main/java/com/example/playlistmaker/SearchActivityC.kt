package com.example.playlistmaker

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.InputMethodManager
import com.google.android.material.textfield.TextInputEditText

class SearchActivityC : BaseActivityC() {

    private lateinit var inputEditText: TextInputEditText
    private lateinit var clearIcon: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search1)

        setupToolbar(R.id.toolbar)

        inputEditText = findViewById(R.id.inputEditText)
        clearIcon = findViewById(R.id.clearIcon)

        savedInstanceState?.let {
            inputEditText.setText(it.getString("SEARCH_QUERY", ""))
        }

        setupEditText()
    }

    private fun setupEditText() {
        inputEditText.inputType = android.text.InputType.TYPE_CLASS_TEXT

        inputEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                clearIcon.visibility = if (s != null && s.isNotEmpty()) {
                    View.VISIBLE
                } else {
                    View.GONE
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        clearIcon.setOnClickListener {
            clearSearchQuery()
        }

        inputEditText.setOnClickListener {
            showKeyboard()
        }
    }

    private fun clearSearchQuery() {
        inputEditText.text?.clear()
        clearIcon.visibility = View.GONE
        hideKeyboard()
    }

    private fun showKeyboard() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(inputEditText, InputMethodManager.SHOW_IMPLICIT)
    }

    private fun hideKeyboard() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(inputEditText.windowToken, 0)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("SEARCH_QUERY", inputEditText.text.toString())
    }
}