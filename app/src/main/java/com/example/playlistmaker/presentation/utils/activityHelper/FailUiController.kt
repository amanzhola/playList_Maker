package com.example.playlistmaker.presentation.utils.activityHelper

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.example.playlistmaker.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class FailUiController( // ❌
    private val activity: Activity,
    private val mainLayout: ViewGroup,
    private val failTextView: TextView
) {
    private val uiHider = ActivityUiHider(activity, mainLayout)

    fun showTemporaryError(isSupport: Boolean) {
        CoroutineScope(Dispatchers.Main).launch {
            showError(isSupport)
            delay(3000)
            hideError()
        }
    }

    private fun showError(isSupport: Boolean) {
        uiHider.hideContent()
        failTextView.text = activity.getString(
            if (isSupport) R.string.supportEmail else R.string.networkFail
        )
        failTextView.visibility = View.VISIBLE
        failTextView.isEnabled = isSupport
    }

    private fun hideError() {
        uiHider.restoreContent()
        failTextView.visibility = View.GONE
    }
}
