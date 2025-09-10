package com.example.playlistmaker.di.playlist

import com.example.playlistmaker.data.repository.playlist.PlaylistRepositoryImpl
import com.example.playlistmaker.domain.repository.playlist.PlaylistRepository
import com.example.playlistmaker.domain.usecases.createPlaylist.CreatePlaylistUseCase
import com.example.playlistmaker.domain.usecases.createPlaylist.UpdatePlaylistUseCase
import com.example.playlistmaker.domain.usecases.createPlaylist.UpdatePlaylistUseCaseImpl
import com.example.playlistmaker.domain.usecases.playlist.AddTrackToPlaylistUseCase
import com.example.playlistmaker.domain.usecases.playlist.ObservePlaylistsUseCase
import com.example.playlistmaker.presentation.createPlaylist.CreatePlaylistViewModel
import com.example.playlistmaker.presentation.media.PlaylistViewModel
import com.example.playlistmaker.presentation.playlistInfo.PlaylistInfoViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val playlistsModule = module {

    single<PlaylistRepository> {
        PlaylistRepositoryImpl(get(), get(), get()) } // add gson for bottom sheet update

    factory {
        CreatePlaylistUseCase(get()) }

    // Экран Create Playlist 🎵➕ Новый плейлист ➕🖼️  ➕for share albums with tracks
    // update + get() for Create Playlist -> edit(updating + use case)
    viewModel {
        CreatePlaylistViewModel(get(), get(), get()) }

    // Экран Медиатеки -> Вкладка "Плейлисты" 🎼
    viewModel {
        PlaylistViewModel(get()) }

    factory {
        ObservePlaylistsUseCase(get())
    }

    // Экран Create Playlist 🎵➕ Новый плейлист ➕🖼️ -> audioPlayer
    factory { AddTrackToPlaylistUseCase(get()) }

    // VM for PlaylistInfoFragment
    viewModel { PlaylistInfoViewModel(get()) }

    // VM for Create Playlist 🎵➕ edit (updating)
    factory<UpdatePlaylistUseCase> {
        UpdatePlaylistUseCaseImpl(get())
    }

}
