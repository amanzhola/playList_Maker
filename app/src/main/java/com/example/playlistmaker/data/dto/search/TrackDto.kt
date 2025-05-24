package com.example.playlistmaker.data.dto.search

data class TrackDto( // 🧱
    val trackName: String,// 🎵
    val artistName: String,// 🎤
    val trackTimeMillis: Long, // ⏱️
    val artworkUrl100: String?, // 💿
    val collectionName: String, // 📀
    val releaseDate: String, // 📅
    val primaryGenreName: String, // 📚
    val country: String, // 🌍
    val previewUrl: String, // 🎧
    val trackId: Int, // 🆔
    var isPlaying: Boolean = false,
    var playTime: String? = "0:00"
//    var playTime: String? = "🕒0:00" // ❌ (☝️ for OK people)
)