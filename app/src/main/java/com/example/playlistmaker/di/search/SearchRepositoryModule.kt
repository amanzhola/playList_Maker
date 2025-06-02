package com.example.playlistmaker.di.search

import com.example.playlistmaker.data.repository.base.SharedPreferencesSearchHistoryRepository
import com.example.playlistmaker.data.repository.search.AudioRepositoryImpl
import com.example.playlistmaker.domain.api.search.AudioRepository
import com.example.playlistmaker.domain.repository.base.SearchHistoryRepository
import org.koin.core.qualifier.named
import org.koin.dsl.module

val searchRepositoryModule = module { // 🔍 from 🏠 🛠️ 🎧 ☁️ 🎥

    // Data // Репозиторий // ✅ AudioRepository
    single<AudioRepository> { AudioRepositoryImpl(get()) }

    // SearchHistoryRepository // ⬅️ 🎶 📜 🎵
    single<SearchHistoryRepository> {
        SharedPreferencesSearchHistoryRepository(get(named("search_history_prefs")))
    }
}
