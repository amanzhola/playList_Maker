package com.example.playlistmaker.domain.api

import com.example.playlistmaker.data.dto.ForecastResponse
import com.example.playlistmaker.data.dto.LocationsResponse
import com.example.playlistmaker.data.network.response.CustomNetworkResponse

interface weatherNetworkClient {
    suspend fun searchLocations(query: String): CustomNetworkResponse<LocationsResponse>
    suspend fun getCurrentWeather(locationId: Int): CustomNetworkResponse<ForecastResponse>
}