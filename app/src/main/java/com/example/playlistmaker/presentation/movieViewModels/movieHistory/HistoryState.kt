package com.example.playlistmaker.presentation.movieViewModels.movieHistory

import com.example.playlistmaker.domain.models.movie.Movie

sealed interface HistoryState {

    object Loading : HistoryState

    data class Content(
        val movies: List<Movie>
    ) : HistoryState

    data class Empty(
        val message: String
    ) : HistoryState
}