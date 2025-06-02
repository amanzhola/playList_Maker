package com.example.playlistmaker.di.weather

import com.example.playlistmaker.domain.api.weather.WeatherInteraction
import com.example.playlistmaker.domain.impl.weather.WeatherInteractionImpl
import org.koin.dsl.module

// WeatherInteraction // 🌧️ ☀️ ⛅ 🌩️ ❄️--> ☁️
val weatherInteractionModule = module {
    single<WeatherInteraction> { WeatherInteractionImpl(get()) }
}
