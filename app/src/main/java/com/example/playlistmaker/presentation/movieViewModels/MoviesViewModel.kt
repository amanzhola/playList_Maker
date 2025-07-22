package com.example.playlistmaker.presentation.movieViewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.playlistmaker.domain.api.movie.MoviesInteraction
import com.example.playlistmaker.domain.models.movie.Movie
import com.example.playlistmaker.domain.usecases.movie.ToggleFavoriteUseCase
import com.example.playlistmaker.domain.util.Resource
import com.example.playlistmaker.utils.SEARCH_DEBOUNCE_DELAY
import com.example.playlistmaker.utils.collectDebouncedIn
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class MoviesViewModel(
    private val moviesInteraction: MoviesInteraction,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase
) : ViewModel() {

    sealed class UiState {
        object Default : UiState()
        object Loading : UiState()
        data class Success(val movies: List<Movie>) : UiState()
        data class Error(val message: String) : UiState()
        object Empty : UiState()
    }

    private val _movies = MutableLiveData<List<Movie>>()
    val movies: LiveData<List<Movie>> = _movies

    private val _uiState = MutableLiveData<UiState>(UiState.Default)
    val uiState: LiveData<UiState> = _uiState

    private val queryFlow = MutableStateFlow("")

    init {

        queryFlow
            .map { it.trim() }
            .collectDebouncedIn(viewModelScope, SEARCH_DEBOUNCE_DELAY) { query ->
                if (query.isEmpty()) {
                    _uiState.value = UiState.Default
                    _movies.value = emptyList()
                } else {
                    searchMovies(query)
                }
            }
    }

    fun onSearchQueryEntered(rawQuery: String) {
        queryFlow.value = rawQuery
    }

    fun searchMovies(query: String) {
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
                            val favoriteIds = toggleFavoriteUseCase.getFavorites()
                            val updatedList = moviesList.map { movie ->
                                movie.copy(inFavorite = favoriteIds.contains(movie.id))
                            }.sortedByDescending { it.inFavorite } // 📥🔄 ❤️🧲🔝 🌟

                            _movies.value = updatedList
                            _uiState.value = UiState.Success(updatedList)
                        }
                    }
                    is Resource.Error -> { // 🧼 🔁  📝
                        _movies.value = emptyList()
                        val msg = result.message ?: "Неизвестная ошибка"
                        _uiState.value = UiState.Error(msg)
                    }
                }
            }
        }
    }

    fun toggleFavorite(movieId: String) {
        val currentMovies = _movies.value ?: return
        val updatedMovies = currentMovies.map { movie ->
            if (movie.id == movieId) {
                val newFavorite = !movie.inFavorite
                toggleFavoriteUseCase(movie.id)
                movie.copy(inFavorite = newFavorite)
            } else movie
        }.sortedByDescending { it.inFavorite } // 📥🔄 ❤️🧲🔝 🌟

        _movies.value = updatedMovies
    }

    fun refreshFavorites() {
        val currentList = _movies.value ?: return

        val updatedList = currentList.map { movie ->
            movie.copy(inFavorite = toggleFavoriteUseCase.isFavorite(movie.id))
        }.sortedByDescending { it.inFavorite } // 📥🔄 ❤️🧲🔝 🌟

        _movies.value = updatedList
    }

    fun setDefaultState() {
        TODO("Not yet implemented")
    }
}
