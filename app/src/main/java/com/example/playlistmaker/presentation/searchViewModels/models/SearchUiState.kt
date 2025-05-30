package com.example.playlistmaker.presentation.searchViewModels.models

import com.example.playlistmaker.domain.models.search.Track
import com.example.playlistmaker.presentation.searchViewModels.ErrorState

data class SearchUiState(
    val query: String = "",
    val isLoading: Boolean = false,
    val isClearIconVisible: Boolean = false,
    val isInputFocused: Boolean = false,
    val showHistory: Boolean = false,
    val searchTracks: List<Track> = emptyList(),
    val historyTracks: List<Track> = emptyList(),
    val error: ErrorState = ErrorState.NONE
) {
    val displayedTracks: List<Track>
        get() = if (showHistory) historyTracks else searchTracks
}
