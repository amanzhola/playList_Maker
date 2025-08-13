package com.example.playlistmaker.presentation.movieViewModels.movieDetails

import com.example.playlistmaker.domain.models.movieDetails.MovieDetails

// 👇 Если у тебя ещё нет Loading/Idle — добавь в AboutState:
sealed class AboutState {
    data object Idle : AboutState()                 // 💤 ничего не запрошено (до loadMovieDetails)
    data object Loading : AboutState()              // ⏳ загружаем
    data class Content(val data: MovieDetails) : AboutState() // ✅ данные
    data class Error(val message: String) : AboutState()      // ❌ ошибка
}
