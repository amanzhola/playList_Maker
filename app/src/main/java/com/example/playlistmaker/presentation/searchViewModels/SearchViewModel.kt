package com.example.playlistmaker.presentation.searchViewModels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.playlistmaker.domain.api.base.SearchHistoryInteraction
import com.example.playlistmaker.domain.api.search.AudioInteraction
import com.example.playlistmaker.domain.models.search.Track
import com.example.playlistmaker.domain.util.Resource
import com.example.playlistmaker.presentation.searchViewModels.models.SearchUiState
import com.example.playlistmaker.utils.SEARCH_DEBOUNCE_DELAY
import com.example.playlistmaker.utils.collectDebouncedIn
import kotlinx.coroutines.flow.MutableStateFlow
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

    private val queryFlow = MutableStateFlow("")

    private val _uiState = MutableLiveData(SearchUiState())
    val uiState: LiveData<SearchUiState> = _uiState

    private val currentState: SearchUiState
        get() = _uiState.value ?: SearchUiState()

    init {
        // 🔁 Подписка на историю
        viewModelScope.launch {
            searchHistoryInteraction.observeHistory().collect { updatedHistory ->
                val query = currentState.query
                val isFocused = currentState.isInputFocused
                val shouldShow = query.isEmpty() && isFocused && updatedHistory.isNotEmpty()

                Log.d("SearchVM", "🎯 History update collected: size=${updatedHistory.size}, show=$shouldShow")

                _uiState.postValue(
                    currentState.copy(
                        historyTracks = updatedHistory,
                        showHistory = shouldShow,
                        error = ErrorState.NONE
                    )
                )
            }
        }

        // ⌛ Debounce поиска
        queryFlow // ✨
            .collectDebouncedIn(viewModelScope, SEARCH_DEBOUNCE_DELAY) { query ->
                if (query.isNotBlank()) {
                    onSearchActionDoneInternal(query)
                }
            }

        // 🕘 Первичная инициализация истории
        viewModelScope.launch {
            val history = searchHistoryInteraction.getHistory()
            val shouldShow = history.isNotEmpty() &&
                    currentState.query.isEmpty() &&
                    currentState.isInputFocused

            Log.d("SearchVM", "🕘 Initial history loaded: size=${history.size}, show=$shouldShow")

            _uiState.value = currentState.copy(
                historyTracks = history,
                showHistory = shouldShow
            )
        }
    }

    fun onQueryChanged(query: String) {
        setSearchQuery(query) // 🎯
        queryFlow.value = query
    }

    private fun setSearchQuery(query: String) {
        val showClear = query.isNotEmpty()
        val isFocused = currentState.isInputFocused
        val history = currentState.historyTracks
        val showHistory = isFocused && query.isEmpty() && history.isNotEmpty()

        _uiState.value = currentState.copy(
            query = query,
            isClearIconVisible = showClear,
            showHistory = showHistory,
            error = ErrorState.NONE
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

    fun onSearchActionDone() {
        val query = currentState.query.trim()
        if (query.isEmpty()) return

        onSearchActionDoneInternal(query)
    }

    private fun onSearchActionDoneInternal(query: String) { // 🔍
        _uiState.postValue(currentState.copy(
            isLoading = true,
            showHistory = false
        ))

        viewModelScope.launch {
            audioInteraction.searchTracks(query).collect { result ->
                when (result) { // 🎯
                    is Resource.Success -> { // ✅
                        val tracks = result.data ?: emptyList()
                        _uiState.postValue(currentState.copy(
                            isLoading = false,
                            searchTracks = tracks,
                            error = if (tracks.isEmpty()) ErrorState.ERROR else ErrorState.NONE
                        ))
                    }

                    is Resource.Error -> { // ⚠️
                        _uiState.postValue(currentState.copy(
                            isLoading = false,
                            searchTracks = emptyList(),
                            error = ErrorState.FAILURE
                        ))
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
        Log.d("SearchViewModel", "Adding to history: $track")
        viewModelScope.launch {
            searchHistoryInteraction.addTrackToHistory(track)
        }
    }

    fun removeTrack(track: Track) {
        if (currentState.showHistory) {
            val updated = currentState.historyTracks.toMutableList().apply {
                removeIf { it.trackId == track.trackId }
            }
            viewModelScope.launch {
                searchHistoryInteraction.saveHistory(updated)
            }
            _uiState.value = currentState.copy(historyTracks = updated)
        } else {
            val updated = currentState.searchTracks.toMutableList().apply {
                removeIf { it.trackId == track.trackId }
            }
            _uiState.value = currentState.copy(searchTracks = updated)
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            searchHistoryInteraction.clearHistory()
        }

        _uiState.value = currentState.copy(
            showHistory = false,
            historyTracks = emptyList()
        )
    }

    fun getTrackHistoryList(): List<Track> = currentState.historyTracks

    fun updateHistoryStateFromFragment(history: List<Track>, show: Boolean) {
        _uiState.value = currentState.copy(
            historyTracks = history,
            showHistory = show
        )
    }

    suspend fun getHistoryTracksFromRepo(): List<Track> {
        return searchHistoryInteraction.getHistory()
    }
}
