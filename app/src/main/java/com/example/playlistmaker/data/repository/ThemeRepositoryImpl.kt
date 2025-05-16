package com.example.playlistmaker.data.repository

import android.content.Context
import androidx.core.content.edit
import com.example.playlistmaker.domain.repository.ThemeRepository

class ThemeRepositoryImpl(private val context: Context) : ThemeRepository {
    private val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)

    override fun isDarkTheme(): Boolean {
        return prefs.getBoolean("dark_theme", false)
    }

    override fun setDarkTheme(enabled: Boolean) {
        prefs.edit() { putBoolean("dark_theme", enabled) }
    }
}