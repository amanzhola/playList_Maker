package com.example.playlistmaker.presentation.launcherViewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.playlistmaker.domain.api.AudioPlayer

@Suppress("UNCHECKED_CAST")
class TrackPreviewViewModelFactory( // 🖼️
    private val audioPlayer: AudioPlayer
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TrackPreviewViewModel::class.java)) {
            return TrackPreviewViewModel(audioPlayer) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}