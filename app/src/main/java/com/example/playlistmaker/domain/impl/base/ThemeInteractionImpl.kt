package com.example.playlistmaker.domain.impl.base

import androidx.appcompat.app.AppCompatDelegate
import com.example.playlistmaker.domain.api.base.ThemeInteraction
import com.example.playlistmaker.domain.repository.base.ThemeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class ThemeInteractionImpl(
    private val repository: ThemeRepository
) : ThemeInteraction {

    private val _darkThemeFlow = MutableStateFlow(repository.isDarkTheme())
    override val darkThemeFlow: StateFlow<Boolean> = _darkThemeFlow

    override fun isDarkTheme(): Boolean = repository.isDarkTheme()

    override fun setDarkTheme(enabled: Boolean) {
        repository.setDarkTheme(enabled)

        _darkThemeFlow.value = enabled
        applyTheme()
    }

    override fun applyTheme() {
        AppCompatDelegate.setDefaultNightMode(
            if (_darkThemeFlow.value) AppCompatDelegate.MODE_NIGHT_YES
            else AppCompatDelegate.MODE_NIGHT_NO
        )
    }
}
