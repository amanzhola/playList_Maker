package com.example.playlistmaker.domain.api.moviesDetails

import com.example.playlistmaker.domain.models.movie.Movie
import com.example.playlistmaker.domain.models.movieCast.MovieCast
import com.example.playlistmaker.domain.models.movieDetails.MovieDetails
import com.example.playlistmaker.domain.util.Resource
import com.example.playlistmaker.domain.util.ResourceMovieDetials
import kotlinx.coroutines.flow.Flow

interface PosterMovieRepository {
    fun searchMovies(expression: String): Flow<ResourceMovieDetials<List<Movie>>>
    fun getMovieDetails(movieId: String): Flow<ResourceMovieDetials<MovieDetails>>
    fun getMovieCast(movieId: String): Flow<Resource<MovieCast>>
}
