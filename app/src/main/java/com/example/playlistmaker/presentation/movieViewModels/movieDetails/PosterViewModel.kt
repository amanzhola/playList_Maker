package com.example.playlistmaker.presentation.movieViewModels.movieDetails

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

private const val KEY_POSTER_URL = "poster_url"

class PosterViewModel(
    initialUrl: String,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _url = MutableStateFlow(savedStateHandle[KEY_POSTER_URL] ?: initialUrl)
    val url: StateFlow<String> = _url.asStateFlow()

    fun setUrl(newUrl: String) {
        _url.value = newUrl
        savedStateHandle[KEY_POSTER_URL] = newUrl
    }
}
