package com.example.playlistmaker.data.createPlaylist

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PlaylistDao {
    @Insert
    suspend fun insert(entity: PlaylistEntity): Long  // вернёт id

    @Query("UPDATE playlists SET trackIdsJson = :trackIdsJson, tracksCount = :count WHERE id = :id")
    suspend fun updateTracks(id: Long, trackIdsJson: String, count: Int)

    // 🔽 НОВОЕ: «живой» поток всех плейлистов (новые сверху)
    @Query("SELECT * FROM playlists ORDER BY id DESC")
    fun observeAll(): Flow<List<PlaylistEntity>>

    // НОВОЕ: adding to bottom sheet update
    @Query("SELECT * FROM playlists WHERE id = :id")
    suspend fun getById(id: Long): PlaylistEntity?
}
