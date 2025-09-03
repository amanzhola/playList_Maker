package com.example.playlistmaker

import android.app.Application
import android.util.Log
import android.view.View
import android.view.ViewGroup
import com.example.playlistmaker.di.appModule
import com.example.playlistmaker.di.db.databaseModule
import com.example.playlistmaker.di.extraOption.extraOptionDataModule
import com.example.playlistmaker.di.extraOption.extraOptionInteractionModule
import com.example.playlistmaker.di.extraOption.extraOptionViewModelModule
import com.example.playlistmaker.di.imageLoaderModule
import com.example.playlistmaker.di.mainActivity.mainActivityModule
import com.example.playlistmaker.di.media.mediaViewModelModule
import com.example.playlistmaker.di.movie.movieDataModule
import com.example.playlistmaker.di.movie.movieInteractionModule
import com.example.playlistmaker.di.movie.movieRepositoryModule
import com.example.playlistmaker.di.movie.movieViewModelModule
import com.example.playlistmaker.di.movie.namesInteractorModule
import com.example.playlistmaker.di.movie.namesRepositoryModule
import com.example.playlistmaker.di.navigation.navigationModule
import com.example.playlistmaker.di.playlist.playlistsModule
import com.example.playlistmaker.di.search.searchDataModule
import com.example.playlistmaker.di.search.searchInteractionModule
import com.example.playlistmaker.di.search.searchRepositoryModule
import com.example.playlistmaker.di.search.searchViewModelModule
import com.example.playlistmaker.di.settingsActivity.settingsActivityDataModule
import com.example.playlistmaker.di.settingsActivity.settingsActivityInteractionModule
import com.example.playlistmaker.di.settingsActivity.settingsActivityViewModelModule
import com.example.playlistmaker.di.weather.weatherDataModule
import com.example.playlistmaker.di.weather.weatherInteractionModule
import com.example.playlistmaker.di.weather.weatherRepositoryModule
import com.example.playlistmaker.domain.api.base.ThemeInteraction
import com.example.playlistmaker.presentation.utils.ThemeLanguageHelper
import org.koin.android.ext.android.get
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext.startKoin

// ☀️ 🔁 🌙 👉 🧼🏗️✅
class App : Application() { // ☀️ 🔁 🌙

    companion object {
        var wasInitialLaunchDone: Boolean = false
    }

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@App)
            modules( // 👉 📝 🔄
                listOf(
                    appModule,
                    weatherDataModule,
                    weatherRepositoryModule,
                    weatherInteractionModule,
                    movieDataModule,
                    movieRepositoryModule,
                    movieInteractionModule,
                    movieViewModelModule,
                    searchDataModule,
                    searchRepositoryModule,
                    searchInteractionModule,
                    searchViewModelModule,
                    extraOptionDataModule,
                    extraOptionInteractionModule,
                    extraOptionViewModelModule,
                    settingsActivityDataModule,
                    settingsActivityInteractionModule,
                    settingsActivityViewModelModule,
                    mainActivityModule,
                    mediaViewModelModule,
                    navigationModule,
                    namesRepositoryModule,      // 👈 добавили
                    namesInteractorModule,      // 👈 добавили
                    imageLoaderModule,          // 👈 для Glide
                    databaseModule,
                    playlistsModule
                )
            )
        }

        // ⛳️ Обязательно: инициализация ThemeLanguageHelper ДО applySavedLanguage
        ThemeLanguageHelper.init(
            theme = get(),       // get<ThemeInteraction> из Koin
            language = get()     // get<LanguageInteraction> из Koin
        )

        // 🌍 Установка языка на основе сохранённого
        ThemeLanguageHelper.applySavedLanguage(this)

        // 3) применяем сохранённую тему (до показа первой Activity)
        get<ThemeInteraction>().applyTheme()

        // 🧼 Ловим крэш-ошибки
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            Log.e("UncaughtException", "Uncaught exception in thread ${thread.name}", throwable)
        }
    }

    private fun View.traverse(action: (View) -> Unit) {
        action(this)
        if (this is ViewGroup) {
            for (i in 0 until childCount) {
                getChildAt(i).traverse(action)
            }
        }
    }
}
