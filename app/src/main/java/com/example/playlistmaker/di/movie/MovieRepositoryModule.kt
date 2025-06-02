package com.example.playlistmaker.di.movie

import com.example.playlistmaker.data.network.movie.IMDbApi
import com.example.playlistmaker.data.repository.base.FavoritesRepositoryImpl
import com.example.playlistmaker.data.repository.movie.MoviesRepositoryImpl
import com.example.playlistmaker.domain.api.movie.MoviesRepository
import com.example.playlistmaker.domain.repository.base.FavoritesRepository
import com.example.playlistmaker.domain.usecases.movie.ToggleFavoriteUseCase
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

val movieRepositoryModule = module {// 🎥 💃 🎬 // 🎥  from 🏠 🔍 🛠️ 🎧 ☁️

    // FavoritesRepository // 📥🔄 ❤️🧲🔝 🌟 (MoviesViewModel)(MoviePager)
    single<FavoritesRepository> { FavoritesRepositoryImpl(get()) }

    // ToggleFavoriteUseCase // 📥🔄 ❤️🧲🔝 🌟 (MoviesViewModel) (MoviePager)
    single { ToggleFavoriteUseCase(get()) }

    // IMDbApi // 🌐 (MoviesViewModel)
    single<IMDbApi> {
        Retrofit.Builder()
            .baseUrl("https://tv-api.com")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(IMDbApi::class.java)
    }

    // MoviesRepository // 🌐 (MoviesViewModel)
    single<MoviesRepository> { MoviesRepositoryImpl(get(), apiKey = "k_zcuw1ytf") }
}
