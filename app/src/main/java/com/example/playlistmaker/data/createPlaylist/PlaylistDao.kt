package com.example.playlistmaker.data.createPlaylist

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.playlistmaker.utils.UPDATE_METADATA_SQL
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

    // ДЛЯ ЭКРАНА «Плейлист»:
    @Query("SELECT * FROM playlists WHERE id = :id")
    fun observeById(id: Long): Flow<PlaylistEntity>

    // ДЛЯ ЭКРАНА «ПлейлистИнфо»:
    @Query("SELECT * FROM playlists") // для проверки «остался ли trackId в плейлистах»
    suspend fun getAll(): List<PlaylistEntity>

    // ДЛЯ ЭКРАНА «ПлейлистИнфо»: удаление
    @Query("DELETE FROM playlists WHERE id = :id")
    suspend fun deleteById(id: Long)

    // ДЛЯ ЭКРАНА «CreatePlaylist»: редактирование
    @Update
    suspend fun update(entity: PlaylistEntity)

    // ✅ самый простой способ не трогаем trackIdsJson/tracksCount:
    @Query(UPDATE_METADATA_SQL)
    suspend fun updateMetadata(
        id: Long,
        name: String,
        description: String?,
        coverPath: String?
    )
}
