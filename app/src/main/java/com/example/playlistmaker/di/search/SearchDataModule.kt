package com.example.playlistmaker.di.search

import android.content.Context
import android.content.SharedPreferences
import com.example.playlistmaker.data.network.search.ITunesApi
import com.example.playlistmaker.data.network.search.RetrofitAudioNetworkClient
import com.example.playlistmaker.data.repository.base.SharedPreferencesSearchHistoryRepository
import com.example.playlistmaker.domain.api.search.AudioNetworkClient
import org.koin.android.ext.koin.androidContext
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

val searchDataModule = module { // 🔍 from 🏠 🛠️ 🎧 ☁️ 🎥
    // Retrofit // 🌐
    single {
        Retrofit.Builder()
            .baseUrl("https://itunes.apple.com") // ⚠️ Без / на конце
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    single<ITunesApi> { get<Retrofit>().create(ITunesApi::class.java) }

    // Network client для поиска аудио // 🌐 Network client
    single<AudioNetworkClient> { RetrofitAudioNetworkClient(get()) }

    // SharedPreferences для истории поиска  👉 📝 📦 💾
    single<SharedPreferences>(named("search_history_prefs")) {
        androidContext().getSharedPreferences(
            SharedPreferencesSearchHistoryRepository.PREFS_NAME, Context.MODE_PRIVATE)} // 👉 📦
}
