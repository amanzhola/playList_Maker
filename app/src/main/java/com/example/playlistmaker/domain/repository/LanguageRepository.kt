package com.example.playlistmaker.domain.repository

interface LanguageRepository {
    fun getLanguage(): String
    fun setLanguage(language: String)
    fun toggleLanguage(): String
}