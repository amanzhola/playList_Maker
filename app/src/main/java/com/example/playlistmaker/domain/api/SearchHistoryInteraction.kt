package com.example.playlistmaker.domain.api

import com.example.playlistmaker.domain.models.Track

interface SearchHistoryInteraction {
    fun getHistory(): List<Track>
    fun addTrackToHistory(track: Track)
    fun saveHistory(tracks: List<Track>)
    fun clearHistory()
    fun subscribeToHistoryChanges(callback: (List<Track>) -> Unit)
    fun unsubscribeFromHistoryChanges()
}
