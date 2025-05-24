package com.example.playlistmaker.domain.api.weather

import com.example.playlistmaker.data.dto.weather.ForecastResponse
import com.example.playlistmaker.data.dto.weather.LocationsResponse
import com.example.playlistmaker.data.network.response.CustomNetworkResponse

interface weatherNetworkClient {
    suspend fun searchLocations(query: String): CustomNetworkResponse<LocationsResponse>
    suspend fun getCurrentWeather(locationId: Int): CustomNetworkResponse<ForecastResponse>
}