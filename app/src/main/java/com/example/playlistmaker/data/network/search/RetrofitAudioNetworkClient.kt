package com.example.playlistmaker.data.network.search

import com.example.playlistmaker.data.dto.search.AudioSearchResponse
import com.example.playlistmaker.data.network.response.CustomNetworkResponse
import com.example.playlistmaker.data.network.safeApiCallAndAdapt
import com.example.playlistmaker.domain.api.search.AudioNetworkClient

class RetrofitAudioNetworkClient(
    private val api: ITunesApi
) : AudioNetworkClient {
    override suspend fun search(term: String): CustomNetworkResponse<AudioSearchResponse> {
        return safeApiCallAndAdapt { api.search(term) }
    }
}