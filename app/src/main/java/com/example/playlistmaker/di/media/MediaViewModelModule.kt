package com.example.playlistmaker.di.media

import com.example.playlistmaker.domain.api.song_db.FavoriteTracksInteractor
import com.example.playlistmaker.domain.usecases.media.MediaLibraryInteractor
import com.example.playlistmaker.presentation.media.FavoriteTracksViewModel
import com.example.playlistmaker.presentation.media.MediaLibraryViewModel
import com.example.playlistmaker.presentation.media.PlaylistViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val mediaViewModelModule = module { // (📀📚)  from 🏠 🔍 🛠️ (🎥 💃) ☁️ 🎧

    // Interactor возвращает список MediaTab без репозитория
    single { MediaLibraryInteractor() }

    // ViewModel, которая использует Interactor
    // Экран медиатеки (общий контейнер с табами)
    viewModel { MediaLibraryViewModel(get()) }

    // Экран Медиатеки -> Вкладка "Плейлисты" 🎼
    viewModel { PlaylistViewModel() }

    // Интерактор избранного 💃❤️✨ (один раз!)
    single { FavoriteTracksInteractor(get()) }


    // Экран Медиатеки -> Вкладка "Избранные треки" 💃❤️✨
    viewModel {
        FavoriteTracksViewModel(
            favoriteTracksInteractor = get()    // FavoriteTracksInteractor
        )
    }
}