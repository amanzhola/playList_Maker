package com.example.playlistmaker.domain.api.song_db

import com.example.playlistmaker.domain.models.search.Track
import kotlinx.coroutines.flow.Flow

interface FavoriteTracksRepository {

    suspend fun addToFavorites(track: Track)

    suspend fun removeFromFavorites(track: Track)

    fun getAllFavorites(): Flow<List<Track>>
}
