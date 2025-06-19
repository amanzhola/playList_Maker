package com.example.playlistmaker.domain.impl.moviesDetials

import com.example.playlistmaker.domain.api.moviesDetials.PosterMovieRepository
import com.example.playlistmaker.domain.repository.base.movieDetails.MoviesInteractor
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

}