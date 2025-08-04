package com.example.playlistmaker.domain.api.base

import com.example.playlistmaker.domain.models.search.Track
import kotlinx.coroutines.flow.Flow

interface SearchHistoryInteraction {
    suspend fun getHistory(): List<Track>
    fun observeHistory(): Flow<List<Track>>
    suspend fun addTrackToHistory(track: Track)
    suspend fun saveHistory(tracks: List<Track>)
    suspend fun clearHistory()
}
