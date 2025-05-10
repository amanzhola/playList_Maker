package com.example.playlistmaker.presentation.audioViewModels

import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.playlistmaker.domain.api.AudioInteraction
import com.example.playlistmaker.domain.repository.SearchHistoryRepository

class SearchViewModelFactory( // 🖼️
    private val audioInteraction: AudioInteraction,
    private val searchHistoryRepository: SearchHistoryRepository,
    private val sharedPreferences: SharedPreferences
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SearchViewModel::class.java)) {
            return SearchViewModel(audioInteraction, searchHistoryRepository, sharedPreferences) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}