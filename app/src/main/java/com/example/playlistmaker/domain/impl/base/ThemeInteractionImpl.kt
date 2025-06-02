package com.example.playlistmaker.domain.impl.base

import com.example.playlistmaker.domain.api.base.ThemeInteraction
import com.example.playlistmaker.domain.repository.base.ThemeRepository
import com.example.playlistmaker.domain.usecases.base.ThemeManager

class ThemeInteractionImpl(
    private val repository: ThemeRepository
) : ThemeInteraction {

    private val themeManager = ThemeManager(repository) // 👈 инициализация

    override fun isDarkTheme(): Boolean = repository.isDarkTheme()

    override fun setDarkTheme(enabled: Boolean) {
        repository.setDarkTheme(enabled)
    }

    override fun toggleTheme() {
        val current = repository.isDarkTheme()
        repository.setDarkTheme(!current)
    }

    override fun applyTheme() {
        themeManager.applyTheme()
    }
}

