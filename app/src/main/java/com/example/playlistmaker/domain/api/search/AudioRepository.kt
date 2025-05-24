package com.example.playlistmaker.domain.api.search

import com.example.playlistmaker.domain.models.search.Track
import com.example.playlistmaker.domain.util.Resource
import kotlinx.coroutines.flow.Flow

interface AudioRepository {
    fun searchTracks(term: String): Flow<Resource<List<Track>>>
}