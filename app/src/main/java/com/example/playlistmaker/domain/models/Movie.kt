package com.example.playlistmaker.domain.models

import com.example.playlistmaker.utils.Identifiable

data class Movie( // 📦 🎥
    override val id: String, // ️🌟
    val image: String, // 💃
    val title: String, // 🌼
    val description: String?,// 👻
    val runtimeStr: String?, // 🕐
    val genres: String? = null, // 📚
    val plot: String? = null, // 📝
    val imDbRating: String? = null, // ⭐
    val year: String?, // 📅
    var inFavorite: Boolean = false,
) : Identifiable<String>
