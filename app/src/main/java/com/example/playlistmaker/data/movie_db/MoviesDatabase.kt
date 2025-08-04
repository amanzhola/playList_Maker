package com.example.playlistmaker.data.movie_db

import androidx.room.Database
import androidx.room.RoomDatabase

// movies DB
@Database(version = 1, entities = [MovieEntity::class], exportSchema = false)
abstract class MoviesDatabase : RoomDatabase() {
    abstract fun movieDao(): MovieDao
}
