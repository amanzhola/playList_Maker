package com.example.playlistmaker.presentation.utils

import androidx.lifecycle.ViewModel
import com.example.playlistmaker.domain.api.base.ThemeInteraction

class ThemeViewModel(private val interaction: ThemeInteraction) : ViewModel() {

    fun toggleTheme(enabled: Boolean) {
        interaction.setDarkTheme(enabled)
        interaction.applyTheme()
    }

    fun isDarkTheme(): Boolean {
        return interaction.isDarkTheme()
    }
}

