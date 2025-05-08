package com.example.playlistmaker.domain.api

import com.example.playlistmaker.data.dto.AudioSearchResponse
import com.example.playlistmaker.data.network.response.CustomNetworkResponse

interface AudioNetworkClient {
    suspend fun search(term: String): CustomNetworkResponse<AudioSearchResponse>
}