package com.example.playlistmaker.data.repository.base

import android.app.Activity
import android.content.Intent
import androidx.core.content.FileProvider
import com.example.playlistmaker.R
import com.example.playlistmaker.data.utils.JsonFileWriter
import com.example.playlistmaker.domain.api.base.NetworkStatusChecker
import com.example.playlistmaker.domain.models.search.Track
import com.example.playlistmaker.domain.repository.base.AudioTracksShare
import com.example.playlistmaker.utils.showFailOrSnack

class AudioTracksImpl(
    private val activity: Activity,
    private val networkStatusChecker: NetworkStatusChecker
) : AudioTracksShare {

    override fun shareTracks(
        tracks: List<Track>,
        messageResId: Int,
        emptyMessageResId: Int,
        fileName: String
    ) {
        if (!networkStatusChecker.isNetworkAvailable()) {
            activity.showFailOrSnack(isSupport = false)
             return // блокировать офлайн-шаринг
        }

        if (tracks.isEmpty()) {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, activity.getString(emptyMessageResId))
            }
            activity.startActivity(Intent.createChooser(intent, activity.getString(R.string.share)))
            return
        }

        val tracksText = buildString {
            tracks.forEachIndexed { i, track ->
                append("${i + 1}. ${track.artistName} – ${track.trackName}\n")
            }
        }
        val message = activity.getString(messageResId) + "\n" + tracksText

        val jsonFile = JsonFileWriter.writeTracksToCache(activity, tracks, fileName)
        val uri = FileProvider.getUriForFile(
            activity, "${activity.packageName}.fileprovider", jsonFile
        )

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/json"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_TEXT, message)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        activity.startActivity(Intent.createChooser(shareIntent, activity.getString(R.string.share)))
    }
}
