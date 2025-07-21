package com.example.playlistmaker.domain.impl.moviesDetails

import com.example.playlistmaker.domain.api.moviesDetails.PosterMovieRepository
import com.example.playlistmaker.domain.models.movie.Movie
import com.example.playlistmaker.domain.models.movieCast.MovieCast
import com.example.playlistmaker.domain.models.movieDetails.MovieDetails
import com.example.playlistmaker.domain.repository.movieDetails.MoviesInteractor
import com.example.playlistmaker.domain.util.Resource
import com.example.playlistmaker.domain.util.ResourceMovieDetials
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map


class MoviesInteractorImpl(
    private val repository: PosterMovieRepository
) : MoviesInteractor {

    override fun searchMovies(expression: String): Flow<Pair<List<Movie>?, String?>> {
        return repository.searchMovies(expression).map { result ->
            when (result) {
                is ResourceMovieDetials.Success -> Pair(result.data, null)
                is ResourceMovieDetials.Error -> Pair(null, result.message)
            }
        }
    }

    override fun getMoviesDetails(movieId: String): Flow<Pair<MovieDetails?, String?>> {
        return repository.getMovieDetails(movieId).map { result ->
            when (result) {
                is ResourceMovieDetials.Success -> Pair(result.data, null)
                is ResourceMovieDetials.Error -> Pair(null, result.message)
            }
        }
    }

    override fun getMovieCast(movieId: String): Flow<Pair<MovieCast?, String?>> {
        return repository.getMovieCast(movieId).map { result ->
            when (result) {
                is Resource.Success -> Pair(result.data, null)
                is Resource.Error -> Pair(null, result.message)
            }
        }
    }
}
