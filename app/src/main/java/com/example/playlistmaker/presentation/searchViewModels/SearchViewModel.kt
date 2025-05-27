package com.example.playlistmaker.presentation.searchViewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.playlistmaker.domain.api.base.SearchHistoryInteraction
import com.example.playlistmaker.domain.api.search.AudioInteraction
import com.example.playlistmaker.domain.models.search.Track
import com.example.playlistmaker.domain.util.Resource
import com.example.playlistmaker.presentation.searchViewModels.models.SearchUiState
import kotlinx.coroutines.launch

enum class ErrorState {
    ERROR,
    FAILURE,
    NONE
}

class SearchViewModel( // 🖼️
    private val audioInteraction: AudioInteraction,
    private val searchHistoryInteraction: SearchHistoryInteraction
) : ViewModel() {

    private val _uiState = MutableLiveData(SearchUiState())
    val uiState: LiveData<SearchUiState> = _uiState

    private val currentState: SearchUiState
        get() = _uiState.value ?: SearchUiState()

    private var ignoreNextHistoryUpdate = false

    init {
        searchHistoryInteraction.subscribeToHistoryChanges { updatedHistory ->
            if (ignoreNextHistoryUpdate) {
                ignoreNextHistoryUpdate = false
                return@subscribeToHistoryChanges
            }

            val shouldShow = currentState.query.isEmpty() &&
                    currentState.isInputFocused && updatedHistory.isNotEmpty()

            _uiState.postValue(
                currentState.copy(
                    historyTracks = updatedHistory,
                    showHistory = shouldShow,
                    error = ErrorState.NONE
                )
            )
        }

        _uiState.value = currentState.copy(
            historyTracks = searchHistoryInteraction.getHistory()
        )
    }

    override fun onCleared() {
        super.onCleared()
        searchHistoryInteraction.unsubscribeFromHistoryChanges()
    }

    fun setSearchQuery(query: String) {
        val showClear = query.isNotEmpty()
        val isFocused = currentState.isInputFocused
        val history = currentState.historyTracks
        val showHistory = isFocused && query.isEmpty() && history.isNotEmpty()

        _uiState.value = currentState.copy(
            query = query,
            isClearIconVisible = showClear,
            showHistory = showHistory,
            error = ErrorState.NONE,
            // не меняем searchTracks, они сохраняются между переходами
        )
    }

    fun setInputFocused(focused: Boolean) {
        val showHistory = focused &&
                currentState.query.isEmpty() &&
                currentState.historyTracks.isNotEmpty()

        _uiState.value = currentState.copy(
            isInputFocused = focused,
            showHistory = showHistory
        )
    }

    fun onSearchActionDone() { // 🔍
        val query = currentState.query.trim()
        if (query.isEmpty()) return

        _uiState.value = currentState.copy(
            isLoading = true,
            showHistory = false
        )

        viewModelScope.launch {
            audioInteraction.searchTracks(query).collect { result ->
                when (result) { // 🎯
                    is Resource.Success -> { // ✅
                        val tracks = result.data ?: emptyList()
                        _uiState.value = currentState.copy(
                            isLoading = false,
                            searchTracks = tracks,
                            error = if (tracks.isEmpty()) ErrorState.ERROR else ErrorState.NONE
                        )
                    }

                    is Resource.Error -> { // ⚠️
                        _uiState.value = currentState.copy(
                            isLoading = false,
                            searchTracks = emptyList(),
                            error = ErrorState.FAILURE
                        )
                    }
                }
            }
        }
    }

    fun clearSearchInput() {
        _uiState.value = currentState.copy(
            query = "",
            isClearIconVisible = false,
            searchTracks = emptyList(),
            error = ErrorState.NONE,
            showHistory = currentState.isInputFocused &&
                    currentState.historyTracks.isNotEmpty()
        )
    }

    fun onTrackClicked(track: Track) { // 🎵
        searchHistoryInteraction.addTrackToHistory(track)
    }

    fun removeTrack(track: Track) {
        if (currentState.showHistory) {
            val updated = currentState.historyTracks.toMutableList().apply {
                removeIf { it.trackId == track.trackId }
            }
            searchHistoryInteraction.saveHistory(updated)
            _uiState.value = currentState.copy(historyTracks = updated)
        } else {
            val updated = currentState.searchTracks.toMutableList().apply {
                removeIf { it.trackId == track.trackId }
            }
            _uiState.value = currentState.copy(searchTracks = updated)
        }
    }

    fun clearHistory() {
        ignoreNextHistoryUpdate = true
        searchHistoryInteraction.clearHistory()

        _uiState.value = currentState.copy(
            showHistory = false,
            historyTracks = emptyList()
        )
    }

    fun getTrackHistoryList(): List<Track> = currentState.historyTracks
}
