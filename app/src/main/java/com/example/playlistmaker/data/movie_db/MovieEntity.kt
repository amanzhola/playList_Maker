package com.example.playlistmaker.data.movie_db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "movie_table")
data class MovieEntity(
    @PrimaryKey
    val id: String,
    val resultType: String? = null,
    val image: String,
    val title: String,
    val description: String
)
