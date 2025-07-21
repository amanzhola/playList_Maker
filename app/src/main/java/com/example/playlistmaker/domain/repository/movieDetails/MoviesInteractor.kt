package com.example.playlistmaker.domain.repository.movieDetails

import com.example.playlistmaker.domain.models.movie.Movie
import com.example.playlistmaker.domain.models.movieCast.MovieCast
import com.example.playlistmaker.domain.models.movieDetails.MovieDetails
import kotlinx.coroutines.flow.Flow

interface MoviesInteractor {
    fun searchMovies(expression: String): Flow<Pair<List<Movie>?, String?>>
    fun getMoviesDetails(movieId: String): Flow<Pair<MovieDetails?, String?>>
    fun getMovieCast(movieId: String): Flow<Pair<MovieCast?, String?>>
}
