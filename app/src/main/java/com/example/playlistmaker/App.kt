package com.example.playlistmaker

import android.app.Application
import com.example.playlistmaker.creator.Creator
import com.example.playlistmaker.data.repository.ThemeRepositoryImpl
import com.example.playlistmaker.domain.usecases.ThemeManager
import com.example.playlistmaker.presentation.utils.ThemeLanguageHelper

// ☀️ 🔁 🌙 👉 🧼🏗️✅
class App : Application() { // ☀️ 🔁 🌙
    lateinit var themeManager: ThemeManager // 😎
        private set

    override fun onCreate() {
        super.onCreate()

        Creator.init(this)

        ThemeLanguageHelper.applySavedLanguage(this)

        val repository = ThemeRepositoryImpl(this)
        themeManager = ThemeManager(repository)
        themeManager.applyTheme()

    }

    fun isDarkThemeEnabled() = themeManager.repository.isDarkTheme()
}