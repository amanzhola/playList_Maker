package com.example.playlistmaker.domain.impl

import com.example.playlistmaker.domain.api.AudioInteraction
import com.example.playlistmaker.domain.api.AudioRepository
import com.example.playlistmaker.domain.api.SearchHistoryInteraction
import com.example.playlistmaker.domain.models.Track
import com.example.playlistmaker.domain.util.Resource
import kotlinx.coroutines.flow.Flow

class AudioInteractionImpl(
    private val repository: AudioRepository,
    private val searchHistoryInteraction: SearchHistoryInteraction
) : AudioInteraction {

    override fun searchTracks(term: String): Flow<Resource<List<Track>>> {
        return repository.searchTracks(term)
    }
}