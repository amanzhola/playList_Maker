package com.example.playlistmaker.domain.impl.base

import com.example.playlistmaker.domain.api.base.SearchHistoryInteraction
import com.example.playlistmaker.domain.models.search.Track
import com.example.playlistmaker.domain.repository.base.SearchHistoryRepository

class SearchHistoryInteractionImpl(
    private val repository: SearchHistoryRepository
) : SearchHistoryInteraction {

    override fun getHistory(): List<Track> {
        return repository.getHistory()
    }

    override fun addTrackToHistory(track: Track) {
        repository.addTrackToHistory(track)
    }

    override fun saveHistory(tracks: List<Track>) {
        repository.saveHistory(tracks)
    }

    override fun clearHistory() {
        repository.clearHistory()
    }

    override fun subscribeToHistoryChanges(callback: (List<Track>) -> Unit) {
        repository.subscribeToHistoryChanges(callback)
    }

    override fun unsubscribeFromHistoryChanges() {
        repository.unsubscribeFromHistoryChanges()
    }
}
