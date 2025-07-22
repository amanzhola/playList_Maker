package com.example.playlistmaker.di.weather

import com.example.playlistmaker.data.network.weather.ForecaApi
import com.example.playlistmaker.data.network.weather.RetrofitWeatherNetworkClient
import com.example.playlistmaker.domain.api.weather.WeatherNetworkClient
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

// Weather API URL
private const val WEATHER_BASE_URL = "https://fnw-us.foreca.com"
private const val WEATHER_TOKEN = "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJodHRwOlwvXC9mbnctdXMuZm9yZWNhLmNvbVwvYXV0aG9yaXplXC90b2tlbiIsImlhdCI6MTc0NjExNTY3MCwiZXhwIjo5OTk5OTk5OTk5LCJuYmYiOjE3NDYxMTU2NzAsImp0aSI6IjdhODU1ODA2MjQ5Yjk1ODQiLCJzdWIiOiJhbG11c2hhX3llcyIsImZtdCI6IlhEY09oakM0MCtBTGpsWVR0amJPaUE9PSJ9.h8xnf_MIoK9F5f4rL922g5Ts8bVsYoSx0hKZgiPqCsg"

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
