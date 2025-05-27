package com.example.playlistmaker.data.repository.base

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.edit
import androidx.core.os.LocaleListCompat
import com.example.playlistmaker.domain.repository.base.LanguageRepository
import java.util.Locale

class LanguageRepositoryImpl(private val context: Context) : LanguageRepository {

    private val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)

    override fun getLanguage(): String {
        return prefs.getString("app_language", Locale.getDefault().language) ?: "en"
    }

    override fun setLanguage(language: String) {
        prefs.edit() { putString("app_language", language) }
        applyLocale(language)
    }

    override fun toggleLanguage(): String {
        val newLang = if (getLanguage() == "en") "ru" else "en"
        setLanguage(newLang)
        return newLang
    }

    private fun applyLocale(language: String) {
        val locale = Locale(language)
        Locale.setDefault(locale)
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.create(locale))
    }
}
