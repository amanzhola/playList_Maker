package com.example.playlistmaker.domain.repository.playlist

import com.example.playlistmaker.domain.models.playlist.Playlist
import com.example.playlistmaker.domain.models.search.Track
import kotlinx.coroutines.flow.Flow

interface PlaylistRepository {
    suspend fun create(name: String, description: String?, coverPath: String?): Long
    fun observeAll(): Flow<List<Playlist>>   // ← для экрана списка
    suspend fun addTrackToPlaylist(playlistId: Long, track: Track): Boolean // НОВОЕ
    suspend fun removeTrackFromPlaylist(playlistId: Long, trackId: Int)

    // ↓ Новое — без этого экрану «Плейлист» неудобно
    fun observePlaylist(id: Long): Flow<Playlist>
    fun observeTracksByIds(ids: List<Int>): Flow<List<Track>>

    // ДЛЯ ЭКРАНА «ПлейлистИнфо»: удаление
    suspend fun deletePlaylist(playlistId: Long)

    // ДЛЯ ЭКРАНА «CreatePlaylist»: редактирование
    suspend fun updatePlaylistMetadata(
        id: Long,
        name: String,
        description: String?,
        coverPath: String?
    )
}
