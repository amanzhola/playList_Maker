package com.example.playlistmaker.domain.impl

import com.example.playlistmaker.domain.api.WeatherInteraction
import com.example.playlistmaker.domain.api.WeatherRepository // Импортируем интерфейс репозитория
import com.example.playlistmaker.domain.models.CurrentWeather
import com.example.playlistmaker.domain.models.ForecastLocation
import com.example.playlistmaker.domain.util.Resource
import kotlinx.coroutines.flow.Flow

class WeatherInteractionImpl(
    private val repository: WeatherRepository
) : WeatherInteraction {

    override fun searchLocations(query: String): Flow<Resource<List<ForecastLocation>>> {
        return repository.searchLocations(query)
    }

    override fun getCurrentWeather(locationId: Int): Flow<Resource<CurrentWeather>> {
        return repository.getCurrentWeather(locationId)
    }
}