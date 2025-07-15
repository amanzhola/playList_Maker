package com.example.playlistmaker.domain.impl.moviesDetails

import com.example.playlistmaker.domain.api.moviesDetails.PosterMovieRepository
import com.example.playlistmaker.domain.repository.movieDetails.MoviesInteractor
import com.example.playlistmaker.domain.util.Resource
import com.example.playlistmaker.domain.util.ResourceMovieDetials
import java.util.concurrent.Executors

class MoviesInteractorImpl(private val repository: PosterMovieRepository) : MoviesInteractor {

    private val executor = Executors.newCachedThreadPool()

    override fun searchMovies(expression: String, consumer: MoviesInteractor.MoviesConsumer) {
        executor.execute {
            when(val resource = repository.searchMovies(expression)) {
                is ResourceMovieDetials.Success -> { consumer.consume(resource.data, null) }
                is ResourceMovieDetials.Error -> { consumer.consume(resource.data, resource.message) }
            }
        }
    }

    override fun getMoviesDetails(movieId: String, consumer: MoviesInteractor.MovieDetailsConsumer) {
        executor.execute {
            when(val resource = repository.getMovieDetails(movieId)) {
                is ResourceMovieDetials.Success -> { consumer.consume(resource.data, null) }
                is ResourceMovieDetials.Error -> { consumer.consume(resource.data, resource.message) }
            }
        }
    }

    override fun getMovieCast(movieId: String, consumer: MoviesInteractor.MovieCastConsumer) {
        executor.execute {
            // Просто вызываем нужный метод Repository
            when(val resource = repository.getMovieCast(movieId)) {
                is Resource.Success -> { consumer.consume(resource.data, null) }
                is Resource.Error -> { consumer.consume(resource.data, resource.message) }
            }
        }
    }

}