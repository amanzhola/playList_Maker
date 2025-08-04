package com.example.playlistmaker.domain.impl.movie_db

import com.example.playlistmaker.domain.api.movie_db.HistoryInteractor
import com.example.playlistmaker.domain.api.movie_db.HistoryRepository
import com.example.playlistmaker.domain.models.movie.Movie
import kotlinx.coroutines.flow.Flow


class HistoryInteractorImpl(
    private val historyRepository: HistoryRepository
) : HistoryInteractor {

    override fun historyMovies(): Flow<List<Movie>> {
        return historyRepository.historyMovies()
    }
}