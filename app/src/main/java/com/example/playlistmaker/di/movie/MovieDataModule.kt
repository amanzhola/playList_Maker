package com.example.playlistmaker.di.movie

import android.content.Context
import android.content.SharedPreferences
import androidx.room.Room
import com.example.playlistmaker.data.local.LocalStorage
import com.example.playlistmaker.data.movie_db.MoviesDatabase
import com.example.playlistmaker.data.repository.base.GsonMovieSerializer
import com.example.playlistmaker.data.repository.base.SharedPrefsMovieStorage
import com.example.playlistmaker.domain.api.movie.MovieSerializer
import com.example.playlistmaker.domain.api.movie.MovieStorageHelper
import org.koin.android.ext.koin.androidContext
import org.koin.core.qualifier.named
import org.koin.dsl.module

val movieDataModule = module { // 🎥 💃 🎬 // 🎥  from 🏠 🔍 🛠️ 🎧 ☁️

    // SharedPreferences для MovieStorageHelper 🎬 🎶 ↔️ 🎵 🎬 с уникальным именем (MovieStorageHelper)
    single<SharedPreferences>(named("selected_movie_prefs")) {
        androidContext().getSharedPreferences("SelectedMoviePrefs", Context.MODE_PRIVATE)
    } // 👉 📦


    // MovieSerializer (MovieStorageHelper) 🎶 ↔️ 🎵 (LauncherActivity) // 🎥💃 ↔️ 💃🎬
    single<MovieSerializer> { GsonMovieSerializer(get()) }

    /// MovieStorageHelper (MovieStorageHelper) 🎶 ↔️ 🎵
    single<MovieStorageHelper> { SharedPrefsMovieStorage(get(named("selected_movie_prefs")), get())}

    // SharedPreferences для избранного // 📥🔄 ❤️🧲🔝 🌟 (MoviesViewModel)
    single<SharedPreferences>(named("favorites_prefs")) { androidContext().getSharedPreferences("favorites_prefs", Context.MODE_PRIVATE)}

    // LocalStorage - зависит от SharedPreferences // 📥🔄 ❤️🧲🔝 🌟 (MoviesViewModel)
    single { LocalStorage(get(named("favorites_prefs"))) }

    // Data Base for Movie
    single {
        Room.databaseBuilder(
            androidContext(),
            MoviesDatabase::class.java, // 👈 новый класс базы данных
            "movies_database.db"        // 👈 можешь поменять имя файла БД
        ).build()
    }
}
