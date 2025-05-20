package com.example.playlistmaker.domain.api

import com.example.playlistmaker.domain.models.Movie

interface MovieStorageHelper {
    fun saveMovie(movie: Movie)
    fun getMovie(): Movie?
    fun saveMovieList(movies: List<Movie>)
    fun getMovieList(): List<Movie>
    fun setCurrentIndex(index: Int)
    fun getCurrentIndex(): Int
    fun parseMoviesFromJson(json: String): List<Movie>?
    fun parseSingleMovieFromJson(json: String): Movie?
}

