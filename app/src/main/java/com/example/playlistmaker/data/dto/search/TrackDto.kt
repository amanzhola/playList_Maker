package com.example.playlistmaker.data.dto.search

// TrackDto.kt — только то, что реально приходит из API
data class TrackDto( // 🧱
    val trackId: Int, // 🆔 // обычно есть
    val trackName: String? = null,// 🎵
    val artistName: String? = null, // 🎤
    val trackTimeMillis: Long? = null,// ⏱️ // иногда отсутствует
    val artworkUrl100: String? = null, // 💿
    val collectionName: String? = null,  // 📀
    val releaseDate: String? = null, // 📅
    val primaryGenreName: String? = null, // 📚
    val country: String? = null, // 🌍
    val previewUrl: String? = null // 🎧
)