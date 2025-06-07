package com.example.playlistmaker.di.weather

import com.example.playlistmaker.data.repository.weather.WeatherRepositoryImpl
import com.example.playlistmaker.domain.api.weather.WeatherRepository
import org.koin.dsl.module

// WeatherRepository // 🌧️ ☀️ ⛅ 🌩️ ❄️--> ☁️
val weatherRepositoryModule = module {
    single<WeatherRepository> { WeatherRepositoryImpl(get()) }
}
