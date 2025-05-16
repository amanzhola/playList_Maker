package com.example.playlistmaker.domain.api

import com.example.playlistmaker.domain.models.CurrentWeather
import com.example.playlistmaker.domain.models.ForecastLocation
import com.example.playlistmaker.domain.util.Resource
import kotlinx.coroutines.flow.Flow

interface WeatherInteraction {

    fun searchLocations(query: String): Flow<Resource<List<ForecastLocation>>>
    fun getCurrentWeather(locationId: Int): Flow<Resource<CurrentWeather>>
}