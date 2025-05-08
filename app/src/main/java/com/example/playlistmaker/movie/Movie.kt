package com.example.playlistmaker.movie

data class Movie( // 📦 🎥
    val id: String, // ️🌟
    val image: String, // 💃
    val title: String, // 🌼
    val description: String,// 👻
    val runtimeStr: String, // 🕐
    val genres: String? = null, // 📚
    val plot: String? = null, // 📝
    val imDbRating: String? = null // ⭐
)

