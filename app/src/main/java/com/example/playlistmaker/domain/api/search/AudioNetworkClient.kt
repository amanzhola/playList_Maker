package com.example.playlistmaker.domain.api.search

import com.example.playlistmaker.data.dto.search.AudioSearchResponse
import com.example.playlistmaker.data.network.response.CustomNetworkResponse

interface AudioNetworkClient {
    suspend fun search(term: String): CustomNetworkResponse<AudioSearchResponse>
}