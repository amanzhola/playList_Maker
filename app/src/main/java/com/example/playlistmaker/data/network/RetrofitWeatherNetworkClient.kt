package com.example.playlistmaker.data.network

import com.example.playlistmaker.data.dto.ForecastResponse
import com.example.playlistmaker.data.dto.LocationsResponse
import com.example.playlistmaker.data.network.response.CustomNetworkResponse
import com.example.playlistmaker.domain.api.weatherNetworkClient

class RetrofitWeatherNetworkClient(
    private val token: String,
    private val forecaApi: ForecaApi
) : weatherNetworkClient {

    override suspend fun searchLocations(query: String): CustomNetworkResponse<LocationsResponse> {
        return safeApiCallAndAdapt { forecaApi.getLocations(token, query) }
    }

    override suspend fun getCurrentWeather(locationId: Int): CustomNetworkResponse<ForecastResponse> {
        return safeApiCallAndAdapt { forecaApi.getForecast(token, locationId) }
    }
}