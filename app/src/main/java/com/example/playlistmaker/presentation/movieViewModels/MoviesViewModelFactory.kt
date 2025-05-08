package com.example.playlistmaker.presentation.movieViewModels

import MoviesInteraction
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class MoviesViewModelFactory(private val moviesInteractor: MoviesInteraction) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MoviesViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MoviesViewModel(moviesInteractor) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}