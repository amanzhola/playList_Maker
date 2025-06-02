package com.example.playlistmaker.domain.impl.search

import com.example.playlistmaker.domain.api.base.SearchHistoryInteraction
import com.example.playlistmaker.domain.api.search.AudioInteraction
import com.example.playlistmaker.domain.api.search.AudioRepository
import com.example.playlistmaker.domain.models.search.Track
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