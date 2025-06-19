package com.example.playlistmaker.domain.repository.base.movieDetails

import com.example.playlistmaker.domain.models.movie.Movie
import com.example.playlistmaker.domain.models.movieDetails.MovieDetails
import com.example.playlistmaker.domain.util.ResourceMovieDetials

interface MoviesRepository {
    fun searchMovies(expression: String): ResourceMovieDetials<List<Movie>>
    fun getMovieDetails(movieId: String): ResourceMovieDetials<MovieDetails>
}