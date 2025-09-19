package com.example.playlistmaker.domain.usecases.playlist

import com.example.playlistmaker.domain.models.playlist.Playlist
import com.example.playlistmaker.domain.models.search.Track
import com.example.playlistmaker.domain.repository.playlist.PlaylistRepository

class AddTrackToPlaylistUseCase(private val repo: PlaylistRepository) {
    suspend operator fun invoke(playlist: Playlist, track: Track): Boolean {
        return repo.addTrackToPlaylist(playlist.id, track)
    }

    // for CreatePlaylistViewModel call off ImportPreviewFragment
    suspend operator fun invoke(playlistId: Long, track: Track): Boolean {
        return repo.addTrackToPlaylist(playlistId, track)
    }
}

