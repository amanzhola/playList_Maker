package com.example.playlistmaker.domain.usecases.createPlaylist

import com.example.playlistmaker.domain.repository.playlist.PlaylistRepository

class CreatePlaylistUseCase(private val repo: PlaylistRepository) {
    suspend operator fun invoke(name: String, description: String?, coverPath: String?) =
        repo.create(name, description, coverPath)
}