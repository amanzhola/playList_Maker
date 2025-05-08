package com.example.playlistmaker.creator

import MoviesInteraction
import MoviesRepositoryImpl
import android.content.Context
import com.example.playlistmaker.data.network.ForecaApi
import com.example.playlistmaker.data.network.IMDbApi
import com.example.playlistmaker.data.network.ITunesApi
import com.example.playlistmaker.data.network.RetrofitAudioNetworkClient
import com.example.playlistmaker.data.network.RetrofitWeatherNetworkClient
import com.example.playlistmaker.data.repository.AudioRepositoryImpl
import com.example.playlistmaker.data.repository.SharedPreferencesSearchHistoryRepository
import com.example.playlistmaker.data.repository.WeatherRepositoryImpl
import com.example.playlistmaker.domain.api.AudioInteraction
import com.example.playlistmaker.domain.api.AudioRepository
import com.example.playlistmaker.domain.api.MoviesRepository
import com.example.playlistmaker.domain.api.WeatherInteraction
import com.example.playlistmaker.domain.api.WeatherRepository
import com.example.playlistmaker.domain.api.weatherNetworkClient
import com.example.playlistmaker.domain.impl.AudioInteractionImpl
import com.example.playlistmaker.domain.impl.MoviesInteractionImpl
import com.example.playlistmaker.domain.impl.WeatherInteractionImpl
import com.example.playlistmaker.domain.repository.SearchHistoryRepository
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object Creator {

    private fun getImdbService(): IMDbApi {
        val imdbBaseUrl = "https://tv-api.com"
        val retrofit = Retrofit.Builder()
            .baseUrl(imdbBaseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        return retrofit.create(IMDbApi::class.java)
    }

    private fun getMoviesRepository(): MoviesRepository {
        val apiKey = "k_zcuw1ytf"
        val imdbService = getImdbService()
        return MoviesRepositoryImpl(imdbService, apiKey)
    }

    fun provideMoviesInteraction(): MoviesInteraction {
        return MoviesInteractionImpl(getMoviesRepository())
    }

    // --- Методы для погоды ---

    private fun getWeatherService(): ForecaApi {
        val weatherBaseUrl = "https://fnw-us.foreca.com"
        val retrofit = Retrofit.Builder()
            .baseUrl(weatherBaseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        return retrofit.create(ForecaApi::class.java)
    }

    private fun getWeatherNetworkClient(): weatherNetworkClient {
        val weatherService = getWeatherService()
        val token = "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJodHRwOlwvXC9mbnctdXMuZm9yZWNhLmNvbVwvYXV0aG9yaXplXC90b2tlbiIsImlhdCI6MTc0NjExNTY3MCwiZXhwIjo5OTk5OTk5OTk5LCJuYmYiOjE3NDYxMTU2NzAsImp0aSI6IjdhODU1ODA2MjQ5Yjk1ODQiLCJzdWIiOiJhbG11c2hhX3llcyIsImZtdCI6IlhEY09oakM0MCtBTGpsWVR0amJPaUE9PSJ9.h8xnf_MIoK9F5f4rL922g5Ts8bVsYoSx0hKZgiPqCsg"
        return RetrofitWeatherNetworkClient(token, weatherService)
    }

    private fun getWeatherRepository(): WeatherRepository {
        val networkClient = getWeatherNetworkClient()
        return WeatherRepositoryImpl(networkClient)
    }

    fun provideWeatherInteraction(): WeatherInteraction {
        val repository = getWeatherRepository()
        return WeatherInteractionImpl(repository)
    }

    // --- Методы для аудио ---

    // --- Методы для iTunes API ---
    private fun getITunesService(): ITunesApi {
        val baseUrl = "https://itunes.apple.com"
        val retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        return retrofit.create(ITunesApi::class.java)
    }

    private fun getAudioNetworkClient(): RetrofitAudioNetworkClient {
        val iTunesApi = getITunesService()
        return RetrofitAudioNetworkClient(iTunesApi)
    }

    private fun getAudioRepository(): AudioRepository {
        return AudioRepositoryImpl(getITunesService())
    }

    // --- Метод для получения SearchHistoryRepository ---
    fun getSearchHistoryRepository(context: Context): SearchHistoryRepository {
        val sharedPreferences = context.getSharedPreferences("search_history", Context.MODE_PRIVATE)
        return SharedPreferencesSearchHistoryRepository(sharedPreferences)
    }

    // --- Метод для предоставления AudioInteraction с учетом SearchHistoryRepository ---
    fun provideAudioInteraction(context: Context): AudioInteraction {
        val audioRepository = getAudioRepository()
        val searchHistoryRepository = getSearchHistoryRepository(context)
        return AudioInteractionImpl(audioRepository, searchHistoryRepository)
    }

}