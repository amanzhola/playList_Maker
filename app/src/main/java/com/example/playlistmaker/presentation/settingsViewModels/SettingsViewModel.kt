package com.example.playlistmaker.presentation.settingsViewModels

import androidx.lifecycle.ViewModel
import com.example.playlistmaker.domain.api.ThemeInteraction

class SettingsViewModel(
    private val themeInteraction: ThemeInteraction
) : ViewModel() {

    fun isDarkTheme(): Boolean = themeInteraction.isDarkTheme()

    fun toggleTheme(enabled: Boolean) {
        themeInteraction.setDarkTheme(enabled)
        themeInteraction.applyTheme()
    }
}