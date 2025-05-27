package com.example.playlistmaker.data.repository.base

import android.app.Activity
import android.content.Intent
import androidx.core.net.toUri
import com.example.playlistmaker.R
import com.example.playlistmaker.domain.repository.base.Support
import com.example.playlistmaker.presentation.utils.activityHelper.FailUiController

class SupportImpl( // 📧
    private val activity: Activity,
    private val failUiController: FailUiController
) : Support {

    override fun writeToSupport() {
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