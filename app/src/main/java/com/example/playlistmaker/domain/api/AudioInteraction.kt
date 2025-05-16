package com.example.playlistmaker.domain.api

import com.example.playlistmaker.domain.models.Track
import com.example.playlistmaker.domain.util.Resource
import kotlinx.coroutines.flow.Flow

interface AudioInteraction {
    fun searchTracks(term: String): Flow<Resource<List<Track>>>
}