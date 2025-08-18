package com.example.playlistmaker.presentation.media

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.playlistmaker.domain.models.playlist.Playlist
import com.example.playlistmaker.domain.usecases.playlist.ObservePlaylistsUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn

class PlaylistViewModel(
    observePlaylists: ObservePlaylistsUseCase
) : ViewModel() {

    private val TAG = "PlaylistVM"

    val playlists: StateFlow<List<Playlist>> =
        observePlaylists()
            .onEach { Log.d(TAG, "flow emit size=${it.size}") }
            .catch { e -> Log.e(TAG, "flow error", e) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )
}
