package com.example.playlistmaker.data.repository.base

import android.app.Activity
import android.content.Intent
import com.example.playlistmaker.R
import com.example.playlistmaker.domain.api.base.NetworkStatusChecker
import com.example.playlistmaker.domain.models.search.Track
import com.example.playlistmaker.domain.repository.base.AudioSingleTrackShare
import com.example.playlistmaker.domain.repository.base.AudioTracksShare
import com.example.playlistmaker.utils.showFailOrSnack

class AudioSingleTrackImpl(
    private val activity: Activity,
    private val shareService: AudioTracksShare,
    private val networkStatusChecker: NetworkStatusChecker
) : AudioSingleTrackShare {

    override fun shareTrackOrNotify(
        track: Track?,
        messageResId: Int,
        emptyMessageResId: Int,
        fileName: String
    ) {
        // мягкое уведомление об офлайне (не блокируем)
        if (!networkStatusChecker.isNetworkAvailable()) {
            activity.showFailOrSnack(isSupport = false)
            // блокировать:
             return
        }

        if (track == null) {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, activity.getString(emptyMessageResId))
            }
            activity.startActivity(Intent.createChooser(intent, activity.getString(R.string.share)))
            return
        }

        shareService.shareTracks(listOf(track), messageResId, emptyMessageResId, fileName)
    }
}
