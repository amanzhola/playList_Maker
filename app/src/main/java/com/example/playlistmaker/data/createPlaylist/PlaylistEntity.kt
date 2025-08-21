package com.example.playlistmaker.data.createPlaylist

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "playlists")
data class PlaylistEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val description: String?,
    val coverPath: String?,      // абсолютный путь к файлу в приватном хранилище
    val trackIdsJson: String,    // "[]"
    val tracksCount: Int         // 0
)
