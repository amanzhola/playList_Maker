package com.example.playlistmaker.data.repository.base

import android.app.Activity
import android.content.Intent
import androidx.core.content.FileProvider
import com.example.playlistmaker.R
import com.example.playlistmaker.data.utils.JsonFileWriter
import com.example.playlistmaker.domain.models.movie.Movie
import com.example.playlistmaker.domain.repository.base.ShareMovie

class ShareMovieImpl(
    private val activity: Activity
) : ShareMovie {

    override fun shareMovieOrNotify(movie: Movie?) {
        if (movie == null) {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, activity.getString(R.string.empty_movie))
            }
            activity.startActivity(Intent.createChooser(intent, null))
            return
        }
        shareMovie(movie)
    }

    private fun shareMovie(movie: Movie) {
        val jsonFile = JsonFileWriter.writeMovieToCache(activity, movie)
        val uri = FileProvider.getUriForFile(activity, "${activity.packageName}.fileprovider", jsonFile)
        val message = "${activity.getString(R.string.share_movie)}\n${movie.title}"

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/json"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_TEXT, message)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        activity.startActivity(Intent.createChooser(shareIntent, null))
    }
}