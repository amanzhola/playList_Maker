package com.example.playlistmaker.di.media

import com.example.playlistmaker.domain.usecases.media.MediaLibraryInteractor
import com.example.playlistmaker.presentation.media.FavouriteTracksViewModel
import com.example.playlistmaker.presentation.media.MediaLibraryViewModel
import com.example.playlistmaker.presentation.media.PlaylistViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val mediaViewModelModule = module { // (📀📚)  from 🏠 🔍 🛠️ (🎥 💃) ☁️ 🎧

    // Interactor возвращает список MediaTab без репозитория
    single { MediaLibraryInteractor() }

    // ViewModel, которая использует Interactor
    viewModel { MediaLibraryViewModel(get()) }

    viewModel { FavouriteTracksViewModel() }

    viewModel { PlaylistViewModel() }
}