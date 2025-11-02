package com.example.playlistmaker.presentation.settingsViewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.playlistmaker.domain.api.base.ThemeInteraction
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class SettingsViewModel(
    private val themeInteraction: ThemeInteraction
) : ViewModel() {

    val darkTheme: StateFlow<Boolean> = themeInteraction.darkThemeFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, themeInteraction.isDarkTheme())

    fun toggleTheme(enabled: Boolean) {
        themeInteraction.setDarkTheme(enabled) // сохранит, эмитнёт, применит
    }
}
