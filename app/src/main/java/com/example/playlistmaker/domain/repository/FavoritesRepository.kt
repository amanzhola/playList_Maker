package com.example.playlistmaker.domain.repository

interface FavoritesRepository {
    fun addToFavorites(id: String)
    fun removeFromFavorites(id: String)
    fun isFavorite(id: String): Boolean
    fun toggleFavorite(id: String): Boolean
    fun getFavorites(): Set<String>
}
