package com.example.playlistmaker.domain.api.movie

import com.example.playlistmaker.domain.models.movie.Movie
import com.example.playlistmaker.domain.util.Resource
import kotlinx.coroutines.flow.Flow // Импортируем Flow

interface MoviesRepository {
    fun searchMovies(expression: String): Flow<Resource<List<Movie>>>
}