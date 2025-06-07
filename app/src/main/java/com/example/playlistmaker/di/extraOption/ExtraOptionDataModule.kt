package com.example.playlistmaker.di.extraOption

import android.content.Context
import android.content.SharedPreferences
import com.example.playlistmaker.data.repository.base.GsonTrackSerializer
import com.example.playlistmaker.data.repository.base.SharedPrefsTrackStorage
import com.example.playlistmaker.data.repository.base.TrackListIntentParserImpl
import com.example.playlistmaker.domain.api.base.TrackSerializer
import com.example.playlistmaker.domain.api.base.TrackStorageHelper
import com.example.playlistmaker.domain.repository.base.TrackListIntentParser
import com.google.gson.Gson
import org.koin.android.ext.koin.androidContext
import org.koin.core.qualifier.named
import org.koin.dsl.module

val extraOptionDataModule = module { // + from 🏠 🔍 🛠️ 🎧 ☁️ 🎥

    // Gson instance (для сериализации треков)
    single { Gson() }

    // SharedPreferences для треков // 👉 📦
    // SharedPreferences для TrackStorageHelper с уникальным именем (TrackStorageHelper) 🎶 ↔️ 🎵
    single<SharedPreferences>(named("selected_track_prefs")) {
        androidContext().getSharedPreferences("SelectedTrackPrefs", Context.MODE_PRIVATE)
    }

    // TrackSerializer
    // use for TrackStorageHelper on TrackDetailsActivity + ExtraOption (??? instead use TrackListIntentParser)
    single<TrackSerializer> { GsonTrackSerializer(get()) } // + LauncherActivity

    // TrackStorageHelper
    single<TrackStorageHelper> {
        SharedPrefsTrackStorage(
            get(named("selected_track_prefs")), // 🧠 правильно указываем prefs
            get<TrackSerializer>()
        )
    } // 👉 📦

    // TrackListIntentParser // transfer Creator.provideTrackListIntentParser() 🎶 ↔️ 🎵
    single<TrackListIntentParser> { TrackListIntentParserImpl() }
}
