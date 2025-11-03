package com.example.playlistmaker.presentation.searchPostersViewModels

import androidx.media3.exoplayer.ExoPlayer

interface ExoPlayerProvider {
    /** Создаёт новый экземпляр ExoPlayer с applicationContext. */
    fun create(): ExoPlayer
}
