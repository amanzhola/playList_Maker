package com.example.playlistmaker.data.dto.movie

data class MovieSearchDto(
    val id: String,
    val resultType: String?,       // <-- Добавляем это поле Movie Data Base
    val description: String? // <-- Актеры ✨ ⭐ 👤
)
