package com.example.playlistmaker.domain.api.base

interface ThemeInteraction {
    fun isDarkTheme(): Boolean
    fun setDarkTheme(enabled: Boolean)
    fun toggleTheme()
    fun applyTheme()
}