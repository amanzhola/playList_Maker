package com.example.playlistmaker.di.playlist

import android.util.Log
import com.example.playlistmaker.data.repository.playlist.PlaylistRepositoryImpl
import com.example.playlistmaker.domain.repository.playlist.PlaylistRepository
import com.example.playlistmaker.domain.usecases.createPlaylist.CreatePlaylistUseCase
import com.example.playlistmaker.domain.usecases.playlist.AddTrackToPlaylistUseCase
import com.example.playlistmaker.domain.usecases.playlist.ObservePlaylistsUseCase
import com.example.playlistmaker.presentation.createPlaylist.CreatePlaylistViewModel
import com.example.playlistmaker.presentation.media.PlaylistViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

// di/PlaylistsModule.kt
val playlistsModule = module {

    Log.i("Koin", "modules loaded: playlistsModule ✅")

    single<PlaylistRepository> {
        Log.i("Koin", "Bind PlaylistRepositoryImpl")
        PlaylistRepositoryImpl(get(), get(), get()) } // add gson for bottom sheet update

    factory {
        Log.i("Koin", "Bind CreatePlaylistUseCase")
        CreatePlaylistUseCase(get()) }

    // Экран Create Playlist 🎵➕ Новый плейлист ➕🖼️
    viewModel {
        Log.i("Koin", "Create CreatePlaylistViewModel")
        CreatePlaylistViewModel(get()) }

    // Экран Медиатеки -> Вкладка "Плейлисты" 🎼
    viewModel {
        Log.i("Koin", "Create PlaylistViewModel")
        PlaylistViewModel(get()) }

    factory {
        Log.i("Koin", "Bind ObservePlaylistsUseCase")
        ObservePlaylistsUseCase(get())
    }

    // Экран Create Playlist 🎵➕ Новый плейлист ➕🖼️ -> audioPlayer
    factory { AddTrackToPlaylistUseCase(get()) }
}
