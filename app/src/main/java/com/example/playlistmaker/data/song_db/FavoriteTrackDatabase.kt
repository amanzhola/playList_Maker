package com.example.playlistmaker.data.song_db


import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [FavoriteTrackEntity::class], version = 2, exportSchema = false)
abstract class FavoriteTrackDatabase : RoomDatabase() {
    abstract fun favoriteTrackDao(): FavoriteTrackDao
}
