package com.example.playlistmaker.presentation.media

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.playlistmaker.domain.models.playlist.Playlist
import com.example.playlistmaker.domain.usecases.playlist.ObservePlaylistsUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class PlaylistViewModel(
    observePlaylists: ObservePlaylistsUseCase
) : ViewModel() {

    val playlists: StateFlow<List<Playlist>> =
        observePlaylists()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )
}
