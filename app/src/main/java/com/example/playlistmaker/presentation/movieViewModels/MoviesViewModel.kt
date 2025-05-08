package com.example.playlistmaker.presentation.movieViewModels

import MoviesInteraction
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.playlistmaker.domain.models.Movie
import com.example.playlistmaker.domain.util.Resource
import kotlinx.coroutines.launch

class MoviesViewModel(
    private val moviesInteraction: MoviesInteraction
) : ViewModel() {

    sealed class UiState {
        data object Default : UiState()
        data object Loading : UiState()
        data class Success(val movies: List<Movie>) : UiState()
        data class Error(val message: String) : UiState()
        data object Empty : UiState()
    }

    private val _movies = MutableLiveData<List<Movie>>()
    val movies: LiveData<List<Movie>> = _movies

    private val _uiState = MutableLiveData<UiState>(UiState.Default)
    val uiState: LiveData<UiState> = _uiState

    fun searchMovies(query: String) {
        if (query.isEmpty()) {
            _movies.value = emptyList()
            _uiState.value = UiState.Default
            return
        }

        _uiState.value = UiState.Loading

        viewModelScope.launch {
            moviesInteraction.searchMovies(query).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        val moviesList = result.data ?: emptyList()

                        if (moviesList.isEmpty()) {
                            _movies.value = emptyList()
                            _uiState.value = UiState.Empty
                        } else {
                            _movies.value = moviesList
                            _uiState.value = UiState.Success(moviesList)
                        }
                    }
                    is Resource.Error -> {
                        _movies.value = emptyList() // 🧼 🔁  📝
                        val errorMessage = result.message ?: "Неизвестная ошибка"
                        _uiState.value = UiState.Error(errorMessage)
                    }
                }
            }
        }

    }
}