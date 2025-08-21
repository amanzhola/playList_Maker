package com.example.playlistmaker.di.db

import androidx.room.Room
import com.example.playlistmaker.data.song_db.FavoriteTrackDatabase
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val databaseModule = module {

    single {
        Room.databaseBuilder(
            androidContext(),
            FavoriteTrackDatabase::class.java,
            "app.db"   // имя БД — любое стабильное
        )
            .addMigrations(
                FavoriteTrackDatabase.MIGRATION_3_4  // НОВОЕ
            )
            .build()
    }
    single { get<FavoriteTrackDatabase>().playlistTrackDao() } // НОВОЕ


    // DAO из этой БД
    single { get<FavoriteTrackDatabase>().favoriteTrackDao() }
    single { get<FavoriteTrackDatabase>().playlistDao() }
}
