package com.example.playlistmaker.presentation.audioPostersViewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.playlistmaker.domain.api.AudioPlayer

class ExtraOptionViewModelFactory( // 🖼️
    private val audioPlayer: AudioPlayer
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ExtraOptionViewModel::class.java)) {
            return ExtraOptionViewModel(audioPlayer) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}