package com.example.playlistmaker.domain.usecases.createPlaylist

import com.example.playlistmaker.domain.repository.playlist.PlaylistRepository


fun interface UpdatePlaylistUseCase {
    suspend operator fun invoke(
        id: Long,
        name: String,
        desc: String?,
        coverPath: String?
    )
}

class UpdatePlaylistUseCaseImpl(
    private val repo: PlaylistRepository
) : UpdatePlaylistUseCase {
    override suspend fun invoke(id: Long, name: String, desc: String?, coverPath: String?) {
        repo.updatePlaylistMetadata(id, name, desc, coverPath)
    }
}
