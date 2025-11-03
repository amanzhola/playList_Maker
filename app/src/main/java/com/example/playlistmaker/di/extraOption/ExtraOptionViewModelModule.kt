package com.example.playlistmaker.di.extraOption

import com.example.playlistmaker.presentation.searchPostersViewModels.ExtraOptionViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val extraOptionViewModelModule = module { // + from 🏠 🔍 🛠️ 🎧 ☁️ 🎥

    // single<AudioPlayerInteraction> { AudioPlayerInteractionImpl() } ViewModel больше не получает AudioPlayerInteraction.
    // Вместо этого Fragment биндинит MusicService и передаёт его в VM через setAudioPlayerControl(...).

    // Экран Аудиоплеера после 💃❤️✨
        viewModel {
        ExtraOptionViewModel(
            favoriteTracksInteractor = get(),   // FavoriteTracksInteractor
            observePlaylists = get(), // ObservePlaylistsUseCase
            addTrackToPlaylist = get(), // AddTrackToPlaylistUseCase
            exoProvider =get()
        )
    }
}
