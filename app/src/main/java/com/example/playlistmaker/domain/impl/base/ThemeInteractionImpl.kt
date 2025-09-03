package com.example.playlistmaker.domain.impl.base

import androidx.appcompat.app.AppCompatDelegate
import com.example.playlistmaker.domain.api.base.ThemeInteraction
import com.example.playlistmaker.domain.repository.base.ThemeRepository

class ThemeInteractionImpl(
    private val repository: ThemeRepository
) : ThemeInteraction {

    override fun isDarkTheme(): Boolean = repository.isDarkTheme()

    override fun setDarkTheme(enabled: Boolean) {
        repository.setDarkTheme(enabled)
    }

    override fun applyTheme() {
        AppCompatDelegate.setDefaultNightMode(
            if (repository.isDarkTheme()) AppCompatDelegate.MODE_NIGHT_YES
            else AppCompatDelegate.MODE_NIGHT_NO
        )
    }
}
