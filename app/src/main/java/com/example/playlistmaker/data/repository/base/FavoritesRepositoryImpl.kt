package com.example.playlistmaker.data.repository.base

import com.example.playlistmaker.data.local.LocalStorage
import com.example.playlistmaker.domain.repository.base.FavoritesRepository

class FavoritesRepositoryImpl(
    private val localStorage: LocalStorage
) : FavoritesRepository {

    override fun addToFavorites(id: String) {
        localStorage.addToFavorites(id)
    }

    override fun removeFromFavorites(id: String) {
        localStorage.removeFromFavorites(id)
    }

    override fun isFavorite(id: String): Boolean {
        return localStorage.getSavedFavorites().contains(id)
    }

    override fun toggleFavorite(id: String): Boolean {
        return if (isFavorite(id)) {
            removeFromFavorites(id)
            false
        } else {
            addToFavorites(id)
            true
        }
    }

    override fun getFavorites(): Set<String> {
        return localStorage.getSavedFavorites()
    }
}
