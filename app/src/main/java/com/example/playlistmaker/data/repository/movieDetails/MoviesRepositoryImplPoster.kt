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

class MoviesRepositoryImplPoster(private val networkClient: NetworkClient,
                                 // Добавили конвертер
                                 private val movieCastConverter: MovieCastConverter,
    ) : PosterMovieRepository {

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

    // Добавили новый метод для получения состава участников
    override fun getMovieCast(movieId: String): Resource<MovieCast> {
        // Поменяли объект dto на нужный Request-объект
        val response = networkClient.doRequest(MovieCastRequest(movieId))
        return when (response.resultCode) {
            -1 -> {
                Resource.Error("Проверьте подключение к интернету")
            }
            200 -> {
                // Осталось написать конвертацию!
//                with(response as MovieCastResponse) {
//                    // step 1
//                    Resource.Success(
//                        data = TODO("Конвертация данных")
//                    )

//                    // step2
//                    Resource.Success(
//                        data = MovieCast(
//                            imdbId = this.imDbId,
//                            fullTitle = this.fullTitle,
//                            directors = this.directors.items.map { director ->
//                                MovieCastPerson(
//                                    id = director.id,
//                                    name = director.name,
//                                    description = director.description,
//                                    image = null,
//                                )
//                            },
//                            others = this.others.flatMap { othersResponse ->
//                                othersResponse.items.map { person ->
//                                    MovieCastPerson(
//                                        id = person.id,
//                                        name = person.name,
//                                        description = "${othersResponse.job} -- ${person.description}",
//                                        image = null,
//                                    )
//                                }
//                            },
//                            writers = this.writers.items.map { writer ->
//                                MovieCastPerson(
//                                    id = writer.id,
//                                    name = writer.name,
//                                    description = writer.description,
//                                    image = null,
//                                )
//                            },
//                            actors = this.actors.map { actor ->
//                                MovieCastPerson(
//                                    id = actor.id,
//                                    name = actor.name,
//                                    description = actor.asCharacter,
//                                    image = actor.image,
//                                )
//                            }
//                        )
//                    )
//                }

                // используем конвертер вместо
                // прямой конвертации
                Resource.Success(
                    data = movieCastConverter.convert(response as MovieCastResponse)
                )
            }
            else -> {
                Resource.Error("Ошибка сервера")
            }
        }
    }
}