package com.example.playlistmaker.presentation.movieViewModels

import com.example.playlistmaker.domain.api.movie.MoviesInteraction
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.playlistmaker.domain.models.movie.Movie
import com.example.playlistmaker.domain.usecases.movie.ToggleFavoriteUseCase
import com.example.playlistmaker.domain.util.Resource
import kotlinx.coroutines.launch

class MoviesViewModel(
    private val moviesInteraction: MoviesInteraction,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase
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

    fun refreshFavorites() {
        val currentList = _movies.value ?: return

        val updatedList = currentList.map { movie ->
            movie.copy(inFavorite = toggleFavoriteUseCase.isFavorite(movie.id))
        }.sortedByDescending { it.inFavorite } // 📥🔄 ❤️🧲🔝 🌟

        _movies.value = updatedList
    }

    fun toggleFavorite(movieId: String) {
        val currentMovies = _movies.value ?: return

        val updatedMovies = currentMovies.map { movie ->
            if (movie.id == movieId) {
                val updatedMovie = movie.copy(inFavorite = !movie.inFavorite)

                toggleFavoriteUseCase(updatedMovie.id)

                updatedMovie
            } else movie
        }.sortedByDescending { it.inFavorite } // 📥🔄 ❤️🧲🔝 🌟

        _movies.value = updatedMovies
    }

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
                        val errorMessage = result.message ?: "Неизвестная ошибка"
                        _uiState.value = UiState.Error(errorMessage)
                    }
                }
            }
        }
    }
}
