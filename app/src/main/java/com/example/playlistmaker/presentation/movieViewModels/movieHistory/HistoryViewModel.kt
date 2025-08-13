package com.example.playlistmaker.presentation.movieViewModels.movieHistory

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.playlistmaker.R
import com.example.playlistmaker.domain.api.movie_db.HistoryInteractor
import com.example.playlistmaker.domain.models.movie.Movie
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn

class HistoryViewModel(
    private val context: Context,
    private val historyInteractor: HistoryInteractor
) : ViewModel() {

    val state: StateFlow<HistoryState> =
        historyInteractor.historyMovies()
            .map< List<Movie>, HistoryState > { movies ->
                if (movies.isEmpty())
                    HistoryState.Empty(context.getString(R.string.nothing_searched_yet))
                else
                    HistoryState.Content(movies)
            }
            .onStart { emit(HistoryState.Loading) } // ⏳ первый экран — лоадер
            .stateIn(
                scope = viewModelScope,
                // Пока есть подписчик — держим активным. Хвост 5с — чтоб не переперезапускалось на коротких паузах.
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = HistoryState.Loading
            )
}
