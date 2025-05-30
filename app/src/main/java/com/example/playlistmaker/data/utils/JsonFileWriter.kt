package com.example.playlistmaker.data.utils

import android.content.Context
import com.example.playlistmaker.domain.models.movie.Movie
import com.example.playlistmaker.domain.models.search.Track
import com.google.gson.Gson
import java.io.File

object JsonFileWriter {

    fun writeTracksToCache(context: Context, tracks: List<Track>, fileName: String = "track_history.json"): File {
        val file = File(context.cacheDir, fileName)
        file.printWriter().use { out ->
            out.print(Gson().toJson(tracks))
        }
        return file
    }

    fun writeMovieToCache(context: Context, movie: Movie, fileName: String = "shared_movie.json"): File {
        val file = File(context.cacheDir, fileName)
        file.printWriter().use { out ->
            out.print(Gson().toJson(movie))
        }
        return file
    }
}