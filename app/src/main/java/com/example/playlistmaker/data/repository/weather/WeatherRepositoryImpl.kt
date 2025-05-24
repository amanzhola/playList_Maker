package com.example.playlistmaker.data.repository.weather

import com.example.playlistmaker.data.dto.weather.CurrentWeatherDto
import com.example.playlistmaker.data.dto.weather.ForecastResponse
import com.example.playlistmaker.data.network.response.CustomNetworkResponse
import com.example.playlistmaker.domain.api.weather.WeatherRepository
import com.example.playlistmaker.domain.api.weather.weatherNetworkClient
import com.example.playlistmaker.domain.models.weather.CurrentWeather
import com.example.playlistmaker.domain.models.weather.ForecastLocation
import com.example.playlistmaker.domain.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class WeatherRepositoryImpl(
    private val networkClient: weatherNetworkClient
) : WeatherRepository {

    override fun searchLocations(query: String): Flow<Resource<List<ForecastLocation>>> = flow {
        val response = networkClient.searchLocations(query)

        if (response.isSuccessful) {

            val locationsResponse = response.body

            val locations = locationsResponse?.locations?.map { locationDto ->
                ForecastLocation(
                    id = locationDto.id,
                    name = locationDto.name,
                    country = locationDto.country
                )
            } ?: emptyList<ForecastLocation>()

            emit(Resource.Success(locations))

        } else {
            val errorMessage = response.errorBody ?: "Ошибка сервера. Код: ${response.code}"
            emit(Resource.Error(errorMessage))
        }
    }

    override fun getCurrentWeather(locationId: Int): Flow<Resource<CurrentWeather>> = flow {

        val response: CustomNetworkResponse<ForecastResponse> = networkClient.getCurrentWeather(locationId)

        if (response.isSuccessful) {

            val forecastResponse: ForecastResponse? = response.body

            val weatherDto: CurrentWeatherDto? = forecastResponse?.current

            val currentWeather = weatherDto?.let { dto ->
                CurrentWeather(
                    temperature = dto.temperature,
                    feelsLikeTemp = dto.feelsLikeTemp
                )
            }

            if (currentWeather != null) {
                emit(Resource.Success(currentWeather))
            } else {
                emit(Resource.Error("Получены пустые данные о погоде (body is null).", null))
            }
        } else {
            val errorMessage = response.errorBody ?: "Ошибка сервера. Код: ${response.code}"
            emit(Resource.Error(errorMessage))
        }
    }
}