package com.example.playlistmaker.data.song_db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.playlistmaker.data.createPlaylist.PlaylistDao
import com.example.playlistmaker.data.createPlaylist.PlaylistEntity
import com.example.playlistmaker.data.playlist.PlaylistTrackDao
import com.example.playlistmaker.data.playlist.PlaylistTrackEntity

@Database( // НОВОЕ PlaylistTrackEntity
    entities = [FavoriteTrackEntity::class, PlaylistEntity::class, PlaylistTrackEntity::class],
    version = 4,                 // ↑ версия
    exportSchema = false
)
abstract class FavoriteTrackDatabase : RoomDatabase() {
    abstract fun favoriteTrackDao(): FavoriteTrackDao
    abstract fun playlistDao(): PlaylistDao
    abstract fun playlistTrackDao(): PlaylistTrackDao // НОВОЕ

    companion object {
        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS playlist_tracks (
                        trackId INTEGER NOT NULL PRIMARY KEY,
                        artworkUrl100 TEXT,
                        trackName TEXT NOT NULL,
                        artistName TEXT NOT NULL,
                        collectionName TEXT,
                        releaseDate TEXT NOT NULL,
                        primaryGenreName TEXT NOT NULL,
                        country TEXT NOT NULL,
                        trackTimeMillis INTEGER NOT NULL,
                        previewUrl TEXT NOT NULL
                    )
                """.trimIndent())
            }
        }
    }
}
