package com.example.playlistmaker.data.repository.movieDetails

import com.example.playlistmaker.data.converters.MovieCastConverter
import com.example.playlistmaker.data.dto.movieCast.MovieCastRequest
import com.example.playlistmaker.data.dto.movieCast.response.MovieCastResponse
import com.example.playlistmaker.data.dto.movieDetails.MovieDetailsRequest
import com.example.playlistmaker.data.dto.movieDetails.MovieDetailsResponse
import com.example.playlistmaker.data.dto.movieDetails.MoviesSearchResponse
import com.example.playlistmaker.data.network.movieDetails.MoviesSearchRequest
import com.example.playlistmaker.data.network.movieDetails.NetworkClient
import com.example.playlistmaker.domain.api.moviesDetails.PosterMovieRepository
import com.example.playlistmaker.domain.models.movie.Movie
import com.example.playlistmaker.domain.models.movieCast.MovieCast
import com.example.playlistmaker.domain.models.movieDetails.MovieDetails
import com.example.playlistmaker.domain.util.Resource
import com.example.playlistmaker.domain.util.ResourceMovieDetials
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class MoviesRepositoryImplPoster(
    private val networkClient: NetworkClient,
    private val movieCastConverter: MovieCastConverter
) : PosterMovieRepository {

    override fun searchMovies(expression: String): Flow<ResourceMovieDetials<List<Movie>>> = flow {
        val response = networkClient.doRequest(MoviesSearchRequest(expression))
        val result = when (response.resultCode) {
            -1 -> ResourceMovieDetials.Error("Проверьте подключение к интернету")
            200 -> {
                with(response as MoviesSearchResponse) {
                    ResourceMovieDetials.Success(results.map {
                        Movie(
                            it.id, it.resultType, it.image, it.title, it.description, year = null,
                            runtimeStr = null,
                            genres = null,
                            plot = null,
                            imDbRating = null,
                            inFavorite = false
                        )
                    })
                }
            }
            else -> ResourceMovieDetials.Error("Ошибка сервера")
        }
        emit(result)
    }.flowOn(Dispatchers.IO)

    override fun getMovieDetails(movieId: String): Flow<ResourceMovieDetials<MovieDetails>> = flow {
        val response = networkClient.doRequest(MovieDetailsRequest(movieId))
        val result = when (response.resultCode) {
            -1 -> ResourceMovieDetials.Error("Проверьте подключение к интернету")
            200 -> {
                with(response as MovieDetailsResponse) {
                    ResourceMovieDetials.Success(
                        MovieDetails(
                            id = id,
                            title = title,
                            imDbRating = imDbRating,
                            year = year,
                            countries = countries,
                            genres = genres,
                            directors = directors,
                            writers = writers,
                            stars = stars,
                            plot = plot
                        )
                    )
                }
            }
            else -> ResourceMovieDetials.Error("Ошибка сервера")
        }
        emit(result)
    }.flowOn(Dispatchers.IO)

    override fun getMovieCast(movieId: String): Flow<Resource<MovieCast>> = flow {
        val response = networkClient.doRequest(MovieCastRequest(movieId))
        val result = when (response.resultCode) {
            -1 -> Resource.Error("Проверьте подключение к интернету")
            200 -> Resource.Success(movieCastConverter.convert(response as MovieCastResponse))
            else -> Resource.Error("Ошибка сервера")
        }
        emit(result)
    }.flowOn(Dispatchers.IO)
}
