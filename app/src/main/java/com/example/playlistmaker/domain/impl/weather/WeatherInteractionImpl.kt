package com.example.playlistmaker.domain.impl.weather

import com.example.playlistmaker.domain.api.weather.WeatherInteraction
import com.example.playlistmaker.domain.api.weather.WeatherRepository // Импортируем интерфейс репозитория
import com.example.playlistmaker.domain.models.weather.CurrentWeather
import com.example.playlistmaker.domain.models.weather.ForecastLocation
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