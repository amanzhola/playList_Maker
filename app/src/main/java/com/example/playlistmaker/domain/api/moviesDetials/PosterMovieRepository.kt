package com.example.playlistmaker.domain.api.moviesDetials

import com.example.playlistmaker.domain.models.movie.Movie
import com.example.playlistmaker.domain.models.movieDetails.MovieDetails
import com.example.playlistmaker.domain.util.ResourceMovieDetials

interface PosterMovieRepository {
    fun searchMovies(expression: String): ResourceMovieDetials<List<Movie>>
    fun getMovieDetails(movieId: String): ResourceMovieDetials<MovieDetails>
}