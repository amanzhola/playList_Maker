package com.example.playlistmaker.data.repository.base

import android.app.Activity
import android.content.Intent
import com.example.playlistmaker.R
import com.example.playlistmaker.domain.repository.base.Share

class ShareImpl(
    private val activity: Activity
) : Share {

    override fun shareApp() {
        val shareMessage = activity.getString(R.string.share_message)
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, shareMessage)
        }
        activity.startActivity(Intent.createChooser(sendIntent, null))
    }
}