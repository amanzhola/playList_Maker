package com.example.playlistmaker

import android.app.Application
import com.example.playlistmaker.data.repository.ThemeRepositoryImpl
import com.example.playlistmaker.domain.usecases.ThemeManager
// ☀️ 🔁 🌙 👉 🧼🏗️✅
class App : Application() { // ☀️ 🔁 🌙
    lateinit var themeManager: ThemeManager // 😎
        private set

    override fun onCreate() {
        super.onCreate()
        val repository = ThemeRepositoryImpl(this)
        themeManager = ThemeManager(repository)
        themeManager.applyTheme()
    }
}