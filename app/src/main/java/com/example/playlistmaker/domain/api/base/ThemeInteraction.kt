package com.example.playlistmaker.domain.api.base

interface ThemeInteraction {
    fun isDarkTheme(): Boolean                // читать флаг из репозитория
    fun setDarkTheme(enabled: Boolean)        // сохранить флаг
    fun applyTheme()                          // применить NightMode по сохранённому флагу
    val darkThemeFlow: kotlinx.coroutines.flow.Flow<Boolean>
}
