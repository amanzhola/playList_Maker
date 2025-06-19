package com.example.playlistmaker.data.repository.movieDetails

import com.example.playlistmaker.data.dto.movieDetails.MovieDetailsRequest
import com.example.playlistmaker.data.dto.movieDetails.MovieDetailsResponse
import com.example.playlistmaker.data.dto.movieDetails.MoviesSearchResponse
import com.example.playlistmaker.data.network.movieDetails.MoviesSearchRequest
import com.example.playlistmaker.data.network.movieDetails.NetworkClient
import com.example.playlistmaker.domain.api.moviesDetials.PosterMovieRepository
import com.example.playlistmaker.domain.models.movie.Movie
import com.example.playlistmaker.domain.models.movieDetails.MovieDetails
import com.example.playlistmaker.domain.util.ResourceMovieDetials

class MoviesRepositoryImplPoster(private val networkClient: NetworkClient) : PosterMovieRepository {

    override fun searchMovies(expression: String): ResourceMovieDetials<List<Movie>> {
        val response = networkClient.doRequest(MoviesSearchRequest(expression))
        return when (response.resultCode) {
            -1 -> {
                ResourceMovieDetials.Error("Проверьте подключение к интернету")
            }
            200 -> {
                with(response as MoviesSearchResponse) {
                    ResourceMovieDetials.Success(results.map {
                        Movie(it.id, it.resultType, it.image, it.title, it.description, year = null)})
                }
            }
            else -> {
                ResourceMovieDetials.Error("Ошибка сервера")
            }
        }
    }

    override fun getMovieDetails(movieId: String): ResourceMovieDetials<MovieDetails> {
        val response = networkClient.doRequest(MovieDetailsRequest(movieId))
        return when (response.resultCode) {
            -1 -> {
                ResourceMovieDetials.Error("Проверьте подключение к интернету")
            }
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
                            plot = plot,
                        )
                    )
                }
            }
            else -> {
                ResourceMovieDetials.Error("Ошибка сервера")

            }
        }
    }
}