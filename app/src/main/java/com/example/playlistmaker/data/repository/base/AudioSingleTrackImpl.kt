package com.example.playlistmaker.data.repository.base

import android.app.Activity
import android.content.Intent
import com.example.playlistmaker.domain.models.search.Track
import com.example.playlistmaker.domain.repository.base.AudioSingleTrackShare
import com.example.playlistmaker.domain.repository.base.AudioTracksShare

class AudioSingleTrackImpl(
    private val activity: Activity,
    private val shareService: AudioTracksShare
) : AudioSingleTrackShare {

    override fun shareTrackOrNotify(
        track: Track?,
        messageResId: Int,
        emptyMessageResId: Int,
        fileName: String
    ) {
        if (track == null) {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, activity.getString(emptyMessageResId))
            }
            activity.startActivity(Intent.createChooser(intent, null))
            return
        }

        shareService.shareTracks(listOf(track), messageResId, emptyMessageResId, fileName)
    }
}