package com.example.playlistmaker.domain.api

import com.example.playlistmaker.domain.models.Movie
import com.example.playlistmaker.domain.util.Resource
import kotlinx.coroutines.flow.Flow // Импортируем Flow

interface MoviesRepository {
    fun searchMovies(expression: String): Flow<Resource<List<Movie>>>
}