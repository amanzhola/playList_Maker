package com.example.playlistmaker.data.network.weather

import com.example.playlistmaker.data.dto.weather.ForecastResponse
import com.example.playlistmaker.data.dto.weather.LocationsResponse
import com.example.playlistmaker.data.network.response.CustomNetworkResponse
import com.example.playlistmaker.data.network.safeApiCallAndAdapt
import com.example.playlistmaker.domain.api.weather.WeatherNetworkClient

class RetrofitWeatherNetworkClient(
    private val token: String,
    private val forecaApi: ForecaApi
) : WeatherNetworkClient {

    override suspend fun searchLocations(query: String): CustomNetworkResponse<LocationsResponse> {
        return safeApiCallAndAdapt { forecaApi.getLocations(token, query) }
    }

    override suspend fun getCurrentWeather(locationId: Int): CustomNetworkResponse<ForecastResponse> {
        return safeApiCallAndAdapt { forecaApi.getForecast(token, locationId) }
    }
}