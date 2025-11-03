package com.example.playlistmaker.di.media

import com.example.playlistmaker.domain.api.song_db.FavoriteTracksInteractor
import com.example.playlistmaker.presentation.media.FavoriteTracksViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val mediaViewModelModule = module { // (📀📚)  from 🏠 🔍 🛠️ (🎥 💃) ☁️ 🎧

    // Interactor возвращает список MediaTab без репозитория
//    single { MediaLibraryInteractor() } // compose migration

    // ViewModel, которая использует Interactor
    // Экран медиатеки (общий контейнер с табами)
//    viewModel { MediaLibraryViewModel(get()) } // compose migration

    // Интерактор избранного 💃❤️✨ (один раз!)
    single { FavoriteTracksInteractor(get()) }


    // Экран Медиатеки -> Вкладка "Избранные треки" 💃❤️✨
    viewModel {
        FavoriteTracksViewModel(
            favoriteTracksInteractor = get()    // FavoriteTracksInteractor
        )
    }
}