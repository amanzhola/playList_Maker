package com.example.playlistmaker.domain.impl.base

import com.example.playlistmaker.domain.api.base.SearchHistoryInteraction
import com.example.playlistmaker.domain.models.search.Track
import com.example.playlistmaker.domain.repository.base.SearchHistoryRepository
import kotlinx.coroutines.flow.Flow

class SearchHistoryInteractionImpl(
    private val repository: SearchHistoryRepository
) : SearchHistoryInteraction {

    override suspend fun getHistory(): List<Track> = repository.getHistory()

    override fun observeHistory(): Flow<List<Track>> = repository.observeHistory()

    override suspend fun addTrackToHistory(track: Track) = repository.addTrackToHistory(track)

    override suspend fun saveHistory(tracks: List<Track>) = repository.saveHistory(tracks)

    override suspend fun clearHistory() = repository.clearHistory()
}
