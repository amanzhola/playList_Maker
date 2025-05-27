package com.example.playlistmaker.domain.impl.movie


import com.example.playlistmaker.domain.api.movie.MoviesInteraction
import com.example.playlistmaker.domain.api.movie.MoviesRepository
import com.example.playlistmaker.domain.models.movie.Movie
import com.example.playlistmaker.domain.util.Resource
import kotlinx.coroutines.flow.Flow

class MoviesInteractionImpl(
    private val repository: MoviesRepository
) : MoviesInteraction {

    override fun searchMovies(query: String): Flow<Resource<List<Movie>>> {
        return repository.searchMovies(query)
    }
}