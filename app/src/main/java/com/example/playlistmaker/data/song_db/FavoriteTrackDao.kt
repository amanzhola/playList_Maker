package com.example.playlistmaker.data.song_db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface FavoriteTrackDao {

    // Добавляем трек в избранное
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addToFavorites(track: FavoriteTrackEntity)

    // Удаляем трек из избранного
    @Delete
    suspend fun removeFromFavorites(track: FavoriteTrackEntity)

    // Получаем все избранные треки
    // Теперь возвращаем треки отсортированные по времени добавления (сначала новые)
    @Query("SELECT * FROM favorite_tracks ORDER BY addedAt DESC")
    suspend fun getAllFavorites(): List<FavoriteTrackEntity>

    // Получаем только идентификаторы избранных треков
    @Query("SELECT trackId FROM favorite_tracks")
    suspend fun getFavoriteTrackIds(): List<Int>
}
