//package com.example.playlistmaker.presentation.utils.activityHelper
//
//import android.app.Activity
//import android.content.Intent
//import androidx.core.content.FileProvider
//import com.example.playlistmaker.R
//import com.example.playlistmaker.data.utils.JsonFileWriter
//import com.example.playlistmaker.domain.models.movie.Movie
//import com.example.playlistmaker.domain.models.search.Track
//
//class AppShareHelper(private val activity: Activity) {
//
//    fun shareApp() {
//        val shareMessage = activity.getString(R.string.share_message)
//        val sendIntent = Intent(Intent.ACTION_SEND).apply {
//            type = "text/plain"
//            putExtra(Intent.EXTRA_TEXT, shareMessage)
//        }
//        activity.startActivity(Intent.createChooser(sendIntent, null))
//    }
//
//    fun shareTracks(
//        tracks: List<Track>,
//        messageResId: Int = R.string.track_share,
//        emptyMessageResId: Int = R.string.track_story_empty,
//        fileName: String = "track_history.json"
//    ) {
//        if (tracks.isEmpty()) {
//            val intent = Intent(Intent.ACTION_SEND).apply {
//                type = "text/plain"
//                putExtra(Intent.EXTRA_TEXT, activity.getString(emptyMessageResId))
//            }
//            activity.startActivity(Intent.createChooser(intent, null))
//            return
//        }
//
//        val tracksText = buildString {
//            tracks.forEachIndexed { i, track ->
//                append("${i + 1}. ${track.artistName} – ${track.trackName}\n")
//            }
//        }
//
//        val message = activity.getString(messageResId) + "\n" + tracksText
//
//        val jsonFile = JsonFileWriter.writeTracksToCache(activity, tracks, fileName)
//        val uri = FileProvider.getUriForFile(activity, "${activity.packageName}.fileprovider", jsonFile)
//
//        val shareIntent = Intent(Intent.ACTION_SEND).apply {
//            type = "application/json"
//            putExtra(Intent.EXTRA_STREAM, uri)
//            putExtra(Intent.EXTRA_TEXT, message)
//            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
//        }
//
//        activity.startActivity(Intent.createChooser(shareIntent, null))
//    }
//
//    fun shareTrackOrNotify(
//        track: Track?,
//        messageResId: Int = R.string.track_share,
//        emptyMessageResId: Int = R.string.empty_track,
//        fileName: String = "single_track.json"
//    ) {
//        if (track == null) {
//            val intent = Intent(Intent.ACTION_SEND).apply {
//                type = "text/plain"
//                putExtra(Intent.EXTRA_TEXT, activity.getString(emptyMessageResId))
//            }
//            activity.startActivity(Intent.createChooser(intent, null))
//            return
//        }
//
//        shareTracks(listOf(track), messageResId, emptyMessageResId, fileName)
//    }
//
//    fun shareMovieOrNotify(movie: Movie?) {
//        if (movie == null) {
//            val intent = Intent(Intent.ACTION_SEND).apply {
//                type = "text/plain"
//                putExtra(Intent.EXTRA_TEXT, activity.getString(R.string.empty_movie))
//            }
//            activity.startActivity(Intent.createChooser(intent, null))
//            return
//        }
//
//        shareMovie(movie)
//    }
//
//    private fun shareMovie(movie: Movie) {
//        val jsonFile = JsonFileWriter.writeMovieToCache(activity,movie)
//        val uri = FileProvider.getUriForFile(activity, "${activity.packageName}.fileprovider", jsonFile)
//        val message = "${activity.getString(R.string.share_movie)}\n${movie.title}"
//
//        val shareIntent = Intent(Intent.ACTION_SEND).apply {
//            type = "application/json"
//            putExtra(Intent.EXTRA_STREAM, uri)
//            putExtra(Intent.EXTRA_TEXT, message)
//            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
//        }
//        activity.startActivity(Intent.createChooser(shareIntent, null))
//    }
//
//
//}