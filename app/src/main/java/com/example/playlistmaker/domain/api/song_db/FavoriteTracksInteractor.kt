package com.example.playlistmaker.domain.api.song_db

import com.example.playlistmaker.domain.models.search.Track
import kotlinx.coroutines.flow.Flow

class FavoriteTracksInteractor(private val repository: FavoriteTracksRepository) {

    suspend fun addToFavorites(track: Track) {
        repository.addToFavorites(track)
    }

    suspend fun removeFromFavorites(track: Track) {
        repository.removeFromFavorites(track)
    }

    fun getAllFavorites(): Flow<List<Track>> {
        return repository.getAllFavorites() // уже отсортировано в БД
    }
}
