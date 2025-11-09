package com.example.playlistmaker.domain.util

interface TextTranslator {
    suspend fun detectLang(text: String): Lang
    suspend fun toEnglish(text: String): String
    suspend fun toRussian(text: String): String
}

enum class Lang { RU, EN, OTHER }

fun isRussian(s: String) = s.any { it in 'А'..'я' || it == 'Ё' || it == 'ё' }