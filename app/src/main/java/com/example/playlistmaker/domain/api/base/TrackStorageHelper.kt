package com.example.playlistmaker.domain.api.base

import com.example.playlistmaker.domain.models.search.Track

interface TrackStorageHelper {
    fun saveTrack(track: Track)
    fun getTrack(): Track?

    fun saveTrackList(tracks: List<Track>)
    fun getTrackList(): List<Track>

    fun setCurrentIndex(index: Int)
    fun getCurrentIndex(): Int

    fun parseTracksFromJson(json: String): List<Track>?
}