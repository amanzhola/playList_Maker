package com.example.playlistmaker.domain.repository

import com.example.playlistmaker.domain.models.Track

interface SearchHistoryRepository {
    fun getHistory(): List<Track>
    fun addTrackToHistory(track: Track)
    fun saveHistory(tracks: List<Track>)
    fun clearHistory()
}