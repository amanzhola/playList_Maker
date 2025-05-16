package com.example.playlistmaker.presentation.utils.activityHelper

import android.app.Activity
import android.content.Intent
import androidx.core.net.toUri
import com.example.playlistmaker.R

class SupportHelper( // 📧
    private val activity: Activity,
    private val failUiController: FailUiController
) {

    fun writeToSupport() {
        val emailUri = "mailto:${activity.getString(R.string.support_email)}?subject=${
            activity.getString(R.string.support_subject)
        }&body=${activity.getString(R.string.support_body)}".toUri()

        val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
            data = emailUri
        }

        try {
            activity.startActivity(emailIntent)
        } catch (e: Exception) { // 👇
            failUiController.showTemporaryError(isSupport = true)
        }
    }
}
