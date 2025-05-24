package com.example.playlistmaker.domain.usecases.base

import androidx.appcompat.app.AppCompatDelegate
import com.example.playlistmaker.domain.repository.base.ThemeRepository

class ThemeManager(val repository: ThemeRepository) {
    fun applyTheme() {
        val isDark = repository.isDarkTheme()
        AppCompatDelegate.setDefaultNightMode(
            if (isDark) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
        )
    }

    fun toggleTheme() {
        val current = repository.isDarkTheme()
        repository.setDarkTheme(!current)
        applyTheme()
    }
}