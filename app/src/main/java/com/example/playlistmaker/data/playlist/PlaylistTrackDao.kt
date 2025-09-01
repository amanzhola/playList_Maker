package com.example.playlistmaker.data.playlist

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PlaylistTrackDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertIgnore(track: PlaylistTrackEntity)

    // ДЛЯ ЭКРАНА «Плейлист»:
    @Query("SELECT * FROM playlist_tracks WHERE trackId IN (:ids)")
    suspend fun getByIds(ids: List<Int>): List<PlaylistTrackEntity>

    // Если удобнее реактивно:
    @Query("SELECT * FROM playlist_tracks WHERE trackId IN (:ids)")
    fun observeByIds(ids: List<Int>): Flow<List<PlaylistTrackEntity>>

    // ДЛЯ ЭКРАНА «ПлейлистИнфо»:
    @Query("DELETE FROM playlist_tracks WHERE trackId = :id")
    suspend fun deleteById(id: Int)
}
