package com.example.playlistmaker.presentation.movieViewModels.movieHistory

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.playlistmaker.R
import com.example.playlistmaker.domain.api.movie_db.HistoryInteractor
import com.example.playlistmaker.domain.models.movie.Movie
import kotlinx.coroutines.launch

class HistoryViewModel(
    private val context: Context,
    private val historyInteractor: HistoryInteractor
) : ViewModel() {

    private val stateLiveData = MutableLiveData<HistoryState>()

    fun observeState(): LiveData<HistoryState> = stateLiveData

    fun fillData() {
        renderState(HistoryState.Loading)
        viewModelScope.launch {
            historyInteractor
                .historyMovies()
                .collect { movies ->
                    Log.d("HistoryViewModel", "Movies count: ${movies.size}")
                    processResult(movies)
                }
        }
    }

    private fun processResult(movies: List<Movie>) {
        if (movies.isEmpty()) {
            renderState(HistoryState.Empty(context.getString(R.string.nothing_searched_yet)))
        } else {
            renderState(HistoryState.Content(movies))
        }
    }

    private fun renderState(state: HistoryState) {
        stateLiveData.postValue(state)
    }
}