package com.example.playlistmaker.presentation.utils.activityHelper

import android.app.Activity
import android.content.Intent
import com.example.playlistmaker.R

class AppShareHelper(private val activity: Activity) { // 🔗

    fun shareApp() {
        val shareMessage = activity.getString(R.string.share_message)
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            putExtra(Intent.EXTRA_TEXT, shareMessage)
            type = "text/plain"
        }
        activity.startActivity(Intent.createChooser(sendIntent, null))
    }
}
