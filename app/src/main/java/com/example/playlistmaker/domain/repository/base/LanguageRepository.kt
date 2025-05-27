package com.example.playlistmaker.domain.repository.base

interface LanguageRepository {
    fun getLanguage(): String
    fun setLanguage(language: String)
    fun toggleLanguage(): String
}