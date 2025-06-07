package com.example.playlistmaker.presentation.utils

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.example.playlistmaker.domain.api.base.LanguageInteraction
import com.example.playlistmaker.domain.api.base.ThemeInteraction
import java.util.Locale

object ThemeLanguageHelper {

    private lateinit var themeInteraction: ThemeInteraction
    private lateinit var languageInteraction: LanguageInteraction

    fun init(theme: ThemeInteraction, language: LanguageInteraction) {
        themeInteraction = theme
        languageInteraction = language
    }

    fun toggleTheme() {
        themeInteraction.toggleTheme()
        themeInteraction.applyTheme()
    } // SegmentManager -> ThemeLanguageHelper.toggleTheme()

    fun toggleLanguage(context: Context): String {
        return languageInteraction.toggleLanguage()
    } // // BaseActivity -> ThemeLanguageHelper.toggleLanguage(this)

    fun applySavedLanguage(context: Context) {
        val currentLang = languageInteraction.getLanguage()
        val locale = Locale(currentLang)
        Locale.setDefault(locale)
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.create(locale))
    } // // App and BaseActivity -> ThemeLanguageHelper.applySavedLanguage(this)

    fun getCurrentLanguage(): String {
        return languageInteraction.getLanguage()
    }

    fun isDarkTheme(): Boolean {
        return themeInteraction.isDarkTheme()
    }
}
