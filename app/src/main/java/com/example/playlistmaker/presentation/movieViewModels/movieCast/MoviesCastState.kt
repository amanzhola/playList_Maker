package com.example.playlistmaker.presentation.movieViewModels.movieCast

import com.example.playlistmaker.ui.core.ui.RVItem

sealed interface MoviesCastState {

    object Loading : MoviesCastState

    data class Error(
        val message: String,
    ) : MoviesCastState

// Вместо объекта MovieCast появились два поля
    data class Content(
        val fullTitle: String,
        // Поменяли тип ячеек на более общий
        val items: List<RVItem>,
    ) : MoviesCastState

}