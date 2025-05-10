package com.example.playlistmaker.utils

import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class UIUpdater(
    private val progressBar: ProgressBar,
    private val placeholderMessage: TextView,
    private val recyclerView: RecyclerView
) {
    fun showLoading() {
        progressBar.visibility = View.VISIBLE
        placeholderMessage.visibility = View.GONE
        recyclerView.visibility = View.GONE
    }

    fun showData() {
        progressBar.visibility = View.GONE
        placeholderMessage.visibility = View.GONE
        recyclerView.visibility = View.VISIBLE
    }

    fun showMessage(message: String) {
        progressBar.visibility = View.GONE
        recyclerView.visibility = View.GONE
        placeholderMessage.visibility = View.VISIBLE
        placeholderMessage.text = message
    }
}