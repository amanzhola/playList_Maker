package com.example.playlistmaker.domain.usecases

import com.example.playlistmaker.domain.repository.FavoritesRepository

class ToggleFavoriteUseCase(private val repository: FavoritesRepository) {
    operator fun invoke(id: String): Boolean {
        return repository.toggleFavorite(id)
    }

    fun getFavorites(): Set<String> = repository.getFavorites()

    fun isFavorite(id: String): Boolean {
        return repository.isFavorite(id)
    }
}
