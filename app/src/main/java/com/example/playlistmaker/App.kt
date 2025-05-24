package com.example.playlistmaker

import android.app.Application
import android.util.Log
import com.example.playlistmaker.creator.Creator
import com.example.playlistmaker.data.repository.base.ThemeRepositoryImpl
import com.example.playlistmaker.domain.usecases.base.ThemeManager
import com.example.playlistmaker.presentation.utils.ThemeLanguageHelper

// ☀️ 🔁 🌙 👉 🧼🏗️✅
class App : Application() { // ☀️ 🔁 🌙
    private lateinit var themeManager: ThemeManager // 😎
        private set

    override fun onCreate() {
        super.onCreate()

        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            Log.e("UncaughtException", "Uncaught exception in thread ${thread.name}", throwable)
        }

        Creator.init(this)

        ThemeLanguageHelper.applySavedLanguage(this)

        val repository = ThemeRepositoryImpl(this)
        themeManager = ThemeManager(repository)
        themeManager.applyTheme()

    }

    fun isDarkThemeEnabled() = themeManager.repository.isDarkTheme()
}