package com.example.playlistmaker.domain.api.movie_db

import com.example.playlistmaker.domain.models.movie.Movie
import kotlinx.coroutines.flow.Flow

interface HistoryRepository {

    fun historyMovies(): Flow<List<Movie>>

}