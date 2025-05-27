package com.example.playlistmaker.domain.api.weather

import com.example.playlistmaker.domain.models.weather.CurrentWeather
import com.example.playlistmaker.domain.models.weather.ForecastLocation
import com.example.playlistmaker.domain.util.Resource
import kotlinx.coroutines.flow.Flow

interface WeatherInteraction {

    fun searchLocations(query: String): Flow<Resource<List<ForecastLocation>>>
    fun getCurrentWeather(locationId: Int): Flow<Resource<CurrentWeather>>
}