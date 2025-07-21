package com.example.playlistmaker.di.weather

import com.example.playlistmaker.data.network.weather.ForecaApi
import com.example.playlistmaker.data.network.weather.RetrofitWeatherNetworkClient
import com.example.playlistmaker.domain.api.weather.WeatherNetworkClient
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

// Weather API URL
private const val WEATHER_BASE_URL = "https://fnw-us.foreca.com"
private const val WEATHER_TOKEN = "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJodHRwOlwvXC9wZmEuZm9yZWNhLmNvbVwvYXV0aG9yaXplXC90b2tlbiIsImlhdCI6MTc1MjgyODM1OSwiZXhwIjo5OTk5OTk5OTk5LCJuYmYiOjE3NTI4MjgzNTksImp0aSI6IjZiZWM2ZWJlMDNmMTZjZDciLCJzdWIiOiJhbWFuemhvbGFpbW92IiwiZm10IjoiWERjT2hqQzQwK0FMamxZVHRqYk9pQT09In0.nACS9EvYRU486JnHkZSfTJCuH0gXmUZBn8VbaMW_kYY"

val weatherDataModule = module { // 🌧️ ☀️ ⛅ 🌩️ ❄️--> ☁️

    // Retrofit + Foreca API // 🌧️ ☀️ ⛅ 🌩️ ❄️--> ☁️
    single<ForecaApi> {
        Retrofit.Builder()
            .baseUrl(WEATHER_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ForecaApi::class.java)
    }

    // WeatherNetworkClient с токеном авторизации
    single<WeatherNetworkClient> { RetrofitWeatherNetworkClient(WEATHER_TOKEN, get()) }
}
