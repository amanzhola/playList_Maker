package com.example.playlistmaker.data.repository

import android.annotation.SuppressLint
import android.content.SharedPreferences
import com.example.playlistmaker.domain.api.MovieSerializer
import com.example.playlistmaker.domain.api.MovieStorageHelper
import com.example.playlistmaker.domain.models.Movie

class SharedPrefsMovieStorage(
    private val sharedPreferences: SharedPreferences,
    private val serializer: MovieSerializer
) : MovieStorageHelper {

    companion object {
        private const val KEY_MOVIE = "selectedMovie"
        private const val KEY_MOVIE_LIST = "movieList"
        private const val KEY_CURRENT_INDEX = "currentMovieIndex"
    }

    @SuppressLint("UseKtx")
    override fun saveMovie(movie: Movie) {
        val json = serializer.serialize(movie)
        sharedPreferences.edit().putString(KEY_MOVIE, json).apply()
    }

    override fun getMovie(): Movie? {
        val json = sharedPreferences.getString(KEY_MOVIE, null)
        return json?.let { serializer.deserialize(it) }
    }

    @SuppressLint("UseKtx")
    override fun saveMovieList(movies: List<Movie>) {
        val json = serializer.serializeList(movies)
        sharedPreferences.edit().putString(KEY_MOVIE_LIST, json).apply()
    }

    override fun getMovieList(): List<Movie> {
        val json = sharedPreferences.getString(KEY_MOVIE_LIST, null)
        return json?.let { serializer.deserializeList(it) } ?: emptyList()
    }

    @SuppressLint("UseKtx")
    override fun setCurrentIndex(index: Int) {
        sharedPreferences.edit().putInt(KEY_CURRENT_INDEX, index).apply()
    }

    override fun getCurrentIndex(): Int {
        return sharedPreferences.getInt(KEY_CURRENT_INDEX, 0)
    }

    override fun parseMoviesFromJson(json: String): List<Movie>? {
        return serializer.deserializeList(json)
    }

    override fun parseSingleMovieFromJson(json: String): Movie? {
        return serializer.deserialize(json)
    }
}

