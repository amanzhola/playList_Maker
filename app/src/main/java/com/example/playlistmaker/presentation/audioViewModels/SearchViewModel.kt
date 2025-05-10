package com.example.playlistmaker.presentation.audioViewModels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.playlistmaker.domain.api.AudioInteraction
import com.example.playlistmaker.domain.api.SearchHistoryInteraction
import com.example.playlistmaker.domain.models.Track
import com.example.playlistmaker.domain.util.Resource
import com.google.gson.Gson
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

    private val _searchQuery = MutableLiveData("")
    val searchQuery: LiveData<String> get() = _searchQuery

    private val _trackList = MutableLiveData<MutableList<Track>>(mutableListOf())
    val trackList: LiveData<MutableList<Track>> = _trackList

    private val _trackHistoryList = MutableLiveData(searchHistoryInteraction.getHistory())
    private val _isHistory = MutableLiveData(false)
    val isHistory: LiveData<Boolean> = _isHistory

    val errorState = MutableLiveData(ErrorState.NONE)

    private val _isInputFocused = MutableLiveData(false)
    val isInputFocused: LiveData<Boolean> = _isInputFocused

    private val _isClearIconVisible = MutableLiveData(false)
    val isClearIconVisible: LiveData<Boolean> = _isClearIconVisible

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    val displayTrackList: LiveData<List<Track>> = MediatorLiveData<List<Track>>().apply {
        fun update() {
            value = if (_isHistory.value == true) {
                _trackHistoryList.value
            } else {
                _trackList.value
            } ?: emptyList()
        }

        addSource(_isHistory) { update() }
        addSource(_trackHistoryList) { update() }
        addSource(_trackList) { update() }
    }

    init {
        searchHistoryInteraction.subscribeToHistoryChanges { updatedHistory ->
            _trackHistoryList.postValue(updatedHistory)
            if (updatedHistory.isEmpty()) _isHistory.postValue(false)
        }

        _trackHistoryList.value = searchHistoryInteraction.getHistory()
    }

    override fun onCleared() {
        super.onCleared()
        searchHistoryInteraction.unsubscribeFromHistoryChanges()
    }

    fun clearSearchInput() {
        _searchQuery.value = ""
        _isClearIconVisible.value = false
        clearTracks()
        errorState.value = ErrorState.NONE
    }

    fun setInputFocused(isFocused: Boolean) {
        _isInputFocused.value = isFocused
        updateHistoryVisibility()
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
        _isClearIconVisible.value = query.isNotEmpty()
        updateHistoryVisibility()
    }

    private fun updateHistoryVisibility() {
        Log.d("SearchViewModel", "👁 History visible: ${_isHistory.value}")
        val query = _searchQuery.value ?: ""
        val history = searchHistoryInteraction.getHistory()
        _isHistory.value = _isInputFocused.value == true && query.isEmpty() && history.isNotEmpty()
    }

    fun onSearchActionDone() {
        val query = _searchQuery.value?.trim() ?: return
        if (query.isNotEmpty()) {
            performSearch(query)
        }
    }

    fun onTrackClicked(track: Track) { // 🎵
        searchHistoryInteraction.addTrackToHistory(track)
        _trackHistoryList.value = searchHistoryInteraction.getHistory()
    }

    fun removeTrack(track: Track) {
        if (_isHistory.value == true) {
            val current = searchHistoryInteraction.getHistory().toMutableList()
            val removed = current.removeIf { it.trackId == track.trackId }
            if (removed) {
                searchHistoryInteraction.saveHistory(current)
                _trackHistoryList.value = current
            }
        } else {
            val currentTracks = _trackList.value?.toMutableList() ?: return
            val index = currentTracks.indexOfFirst { it.trackId == track.trackId }
            if (index >= 0) {
                currentTracks.removeAt(index)
                _trackList.value = currentTracks
            }
        }
    }

    private fun performSearch(query: String) { // 🔍
        _isLoading.value = true
        viewModelScope.launch {
            audioInteraction.searchTracks(query).collect { result: Resource<List<Track>> ->
                _isLoading.value = false
                when (result) { // 🎯
                    is Resource.Success -> {
                        val tracks = result.data ?: emptyList()
                        if (tracks.isEmpty()) { // ⚠️
                            _trackList.value = mutableListOf()
                            errorState.value = ErrorState.ERROR
                        } else { // ✅
                            _trackList.value = tracks.toMutableList()
                            errorState.value = ErrorState.NONE
                        }
                    }

                    is Resource.Error -> {
                        _trackList.value = mutableListOf()
                        errorState.value = ErrorState.FAILURE
                    }
                }
            }
        }
    }

    private fun clearTracks() {
        _trackList.value = mutableListOf()
    }

    fun getTrackHistoryJson(): String {
        val list = _trackHistoryList.value ?: emptyList()
        return Gson().toJson(list)
    }

    fun clearHistory() {
        searchHistoryInteraction.clearHistory()
        _isHistory.value = false
    }
}