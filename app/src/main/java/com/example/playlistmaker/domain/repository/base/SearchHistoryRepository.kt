package com.example.playlistmaker.domain.repository.base

import com.example.playlistmaker.domain.models.search.Track
import kotlinx.coroutines.flow.Flow

// update to avoid using runBlocking which blocks the main thread to have coroutine
interface SearchHistoryRepository {
    suspend fun getHistory(): List<Track>
    suspend fun addTrackToHistory(track: Track)
    suspend fun saveHistory(tracks: List<Track>)
    suspend fun clearHistory()

    // Новый поток с историей
    fun observeHistory(): Flow<List<Track>>
}
