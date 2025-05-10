package com.example.playlistmaker.domain.repository

interface ThemeRepository {
    fun isDarkTheme(): Boolean
    fun setDarkTheme(enabled: Boolean)
}