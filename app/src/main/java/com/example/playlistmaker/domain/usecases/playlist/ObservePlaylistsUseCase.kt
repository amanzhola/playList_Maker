package com.example.playlistmaker.domain.usecases.playlist

import com.example.playlistmaker.domain.models.playlist.Playlist
import com.example.playlistmaker.domain.repository.playlist.PlaylistRepository

import kotlinx.coroutines.flow.Flow

class ObservePlaylistsUseCase(
    private val repo: PlaylistRepository
) {
    operator fun invoke(): Flow<List<Playlist>> = repo.observeAll()
}
