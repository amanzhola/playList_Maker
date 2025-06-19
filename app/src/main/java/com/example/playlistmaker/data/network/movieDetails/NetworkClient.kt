package com.example.playlistmaker.data.network.movieDetails

import com.example.playlistmaker.data.dto.movieDetails.Response

interface NetworkClient {
    fun doRequest(dto: Any): Response
}