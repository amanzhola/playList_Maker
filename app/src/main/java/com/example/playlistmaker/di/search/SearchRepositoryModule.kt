package com.example.playlistmaker.di.search

import com.example.playlistmaker.data.repository.base.SharedPreferencesSearchHistoryRepository
import com.example.playlistmaker.data.repository.search.AudioRepositoryImpl
import com.example.playlistmaker.data.repository.song_db.FavoritesRepositoryImpl
import com.example.playlistmaker.domain.api.search.AudioRepository
import com.example.playlistmaker.domain.api.song_db.FavoriteTracksInteractor
import com.example.playlistmaker.domain.api.song_db.FavoriteTracksRepository
import com.example.playlistmaker.domain.repository.base.SearchHistoryRepository
import org.koin.core.qualifier.named
import org.koin.dsl.module

val searchRepositoryModule = module { // 🔍 from 🏠 🛠️ 🎧 ☁️ 🎥

    // выделение избранных треков 💃❤️✨// Data // Репозиторий // ✅ AudioRepository
    single<AudioRepository> { AudioRepositoryImpl(get(), get()) }
    // get() = AudioNetworkClient
    // get() = FavoriteTrackDao

    // выделение избранных треков 💃❤️✨  // SearchHistoryRepository // ⬅️ 🎶 📜 🎵
    single<SearchHistoryRepository> {
        SharedPreferencesSearchHistoryRepository(
            get(named("search_history_prefs")),
            get() // FavoriteTrackDao
        )
    }

    // Репозиторий избранных треков 💃❤️✨
    single<FavoriteTracksRepository> {
        FavoritesRepositoryImpl(get()) // get() = FavoriteTrackDao
    }

    // Интерактор избранного 💃❤️✨
    single { FavoriteTracksInteractor(get()) }
}
