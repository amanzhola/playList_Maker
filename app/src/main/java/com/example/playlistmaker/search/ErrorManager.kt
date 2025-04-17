package com.example.playlistmaker.search

import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.playlistmaker.R
import com.google.android.material.button.MaterialButton

class ErrorManager(
    private val textView: TextView,
    private val update: MaterialButton,
    private val recyclerView: RecyclerView
) {

    private fun opt() {
        recyclerView.visibility = GONE
        textView.visibility = VISIBLE
    }

    fun showError() {
        opt()
        textView.isEnabled = true
        textView.setText(R.string.searchFail)
        update.visibility = GONE
    }

    fun showFailure(){
        opt()
        textView.isEnabled = false
        textView.setText(R.string.networkFail)
        update.visibility = VISIBLE
        update.setText(R.string.update)
    }

    fun hideError(){
        textView.visibility = GONE
        update.visibility = GONE
        recyclerView.visibility = VISIBLE
    }
}