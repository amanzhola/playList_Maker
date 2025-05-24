package com.example.playlistmaker.domain.api.movie

import com.example.playlistmaker.domain.models.movie.Movie

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

