package com.example.playlistmaker.presentation.movieViewModels

import MoviesInteraction
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.playlistmaker.domain.usecases.ToggleFavoriteUseCase

class MoviesViewModelFactory(private val moviesInteractor: MoviesInteraction,
                             private val toggleFavoriteUseCase: ToggleFavoriteUseCase
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MoviesViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MoviesViewModel(moviesInteractor, toggleFavoriteUseCase) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}