package com.example.playlistmaker.presentation.utils

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.example.playlistmaker.creator.Creator
import java.util.Locale

object ThemeLanguageHelper {

    fun toggleTheme() {
        val interaction = Creator.provideThemeInteraction()
        interaction.toggleTheme()
        interaction.applyTheme()
    }

    fun toggleLanguage(context: Context): String {
        val interaction = Creator.provideLanguageInteraction()
        return interaction.toggleLanguage()
    }

    fun applySavedLanguage(context: Context) {
        val interaction = Creator.provideLanguageInteraction()
        val currentLang = interaction.getLanguage()
        val locale = Locale(currentLang)
        Locale.setDefault(locale)
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.create(locale))
    }

    fun getCurrentLanguage(): String {
        return Creator.provideLanguageInteraction().getLanguage()
    }

    fun isDarkTheme(): Boolean {
        return Creator.provideThemeInteraction().isDarkTheme()
    }
}
