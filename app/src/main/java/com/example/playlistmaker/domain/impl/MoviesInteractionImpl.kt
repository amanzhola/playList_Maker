package com.example.playlistmaker.domain.impl


import MoviesInteraction
import com.example.playlistmaker.domain.api.MoviesRepository
import com.example.playlistmaker.domain.models.Movie
import com.example.playlistmaker.domain.util.Resource
import kotlinx.coroutines.flow.Flow

class MoviesInteractionImpl(
    private val repository: MoviesRepository
) : MoviesInteraction {

    override fun searchMovies(query: String): Flow<Resource<List<Movie>>> {
        return repository.searchMovies(query)
    }
}