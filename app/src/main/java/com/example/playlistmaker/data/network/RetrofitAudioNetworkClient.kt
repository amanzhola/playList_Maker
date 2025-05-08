package com.example.playlistmaker.data.network

import com.example.playlistmaker.data.dto.AudioSearchResponse
import com.example.playlistmaker.data.network.response.CustomNetworkResponse
import com.example.playlistmaker.domain.api.AudioNetworkClient

class RetrofitAudioNetworkClient(
    private val api: ITunesApi
) : AudioNetworkClient {
    override suspend fun search(term: String): CustomNetworkResponse<AudioSearchResponse> {
        return safeApiCallAndAdapt { api.search(term) }
    }
}