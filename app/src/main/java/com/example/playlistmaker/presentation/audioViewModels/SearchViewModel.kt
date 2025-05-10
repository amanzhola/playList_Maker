package com.example.playlistmaker.presentation.audioViewModels

import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.*
import com.example.playlistmaker.domain.api.AudioInteraction
import com.example.playlistmaker.domain.models.Track
import com.example.playlistmaker.domain.repository.SearchHistoryRepository
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
    private val searchHistoryRepository: SearchHistoryRepository,
    private val sharedPreferences: SharedPreferences
) : ViewModel() {

    private val _searchQuery = MutableLiveData("")
    val searchQuery: LiveData<String> get() = _searchQuery

    private val _trackList = MutableLiveData<MutableList<Track>>(mutableListOf())
    val trackList: LiveData<MutableList<Track>> = _trackList

    private val _trackHistoryList = MutableLiveData(searchHistoryRepository.getHistory())
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

    private val prefsChangeListener =
        SharedPreferences.OnSharedPreferenceChangeListener { prefs, key -> // 📦
            if (key == "track_history_list") {
                val json = prefs.getString("track_history_list", "")
                val list = if (!json.isNullOrEmpty()) {
                    Gson().fromJson(json, Array<Track>::class.java).toList()
                } else {
                    emptyList()
                }
                _trackHistoryList.value = list // 📜
                if (list.isEmpty()) _isHistory.value = false
            }
        }

    init {
        sharedPreferences.registerOnSharedPreferenceChangeListener(prefsChangeListener)
        _trackHistoryList.value = searchHistoryRepository.getHistory()
    }

    override fun onCleared() {
        super.onCleared()
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(prefsChangeListener)
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
        val history = searchHistoryRepository.getHistory()
        _isHistory.value = _isInputFocused.value == true && query.isEmpty() && history.isNotEmpty()
    }

    fun onSearchActionDone() {
        val query = _searchQuery.value?.trim() ?: return
        if (query.isNotEmpty()) {
            performSearch(query)
        }
    }

    fun onTrackClicked(track: Track) { // 🎵
        searchHistoryRepository.addTrackToHistory(track)
        _trackHistoryList.value = searchHistoryRepository.getHistory()
    }

    fun removeTrack(track: Track) {
        if (_isHistory.value == true) {
            val current = searchHistoryRepository.getHistory().toMutableList()
            val removed = current.removeIf { it.trackId == track.trackId }
            if (removed) {
                searchHistoryRepository.saveHistory(current)
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
}