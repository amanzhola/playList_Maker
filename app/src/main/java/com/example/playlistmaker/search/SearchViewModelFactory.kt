package com.example.playlistmaker.search

import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class SearchViewModelFactory(
    private val apiService: ITunesApi,
    private val searchHistory: SearchHistory,
    private val sharedPreferences: SharedPreferences
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SearchViewModel::class.java)) {
            return SearchViewModel(apiService, searchHistory, sharedPreferences) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}