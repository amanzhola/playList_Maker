package com.example.playlistmaker.domain.repository.base

import com.example.playlistmaker.domain.models.search.Track

interface SearchHistoryRepository {
    fun getHistory(): List<Track>
    fun addTrackToHistory(track: Track)
    fun saveHistory(tracks: List<Track>)
    fun clearHistory()
    fun subscribeToHistoryChanges(callback: (List<Track>) -> Unit)
    fun unsubscribeFromHistoryChanges()
}