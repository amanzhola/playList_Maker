package com.example.playlistmaker.domain.api.moviesDetails

import com.example.playlistmaker.domain.models.movie.Movie
import com.example.playlistmaker.domain.models.movieCast.MovieCast
import com.example.playlistmaker.domain.models.movieDetails.MovieDetails
import com.example.playlistmaker.domain.util.Resource
import com.example.playlistmaker.domain.util.ResourceMovieDetials

interface PosterMovieRepository {
    fun searchMovies(expression: String): ResourceMovieDetials<List<Movie>>
    fun getMovieDetails(movieId: String): ResourceMovieDetials<MovieDetails>
    fun getMovieCast(movieId: String): Resource<MovieCast>
}