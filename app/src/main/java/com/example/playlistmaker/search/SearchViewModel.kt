package com.example.playlistmaker.search

import android.content.SharedPreferences
import androidx.lifecycle.*
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

enum class ErrorState {
    ERROR,
    FAILURE,
    NONE
}

class SearchViewModel(
    private val apiService: ITunesApi,
    private val searchHistory: SearchHistory,
    private val sharedPreferences: SharedPreferences
) : ViewModel() {

    private val _searchQuery = MutableLiveData<String>()
    val searchQuery: LiveData<String> get() = _searchQuery

    private val _trackList = MutableLiveData<MutableList<Track>>()
    val trackList: LiveData<MutableList<Track>> get() = _trackList

    private val _trackHistoryList = MutableLiveData<List<Track>>()
    private val _isHistory = MutableLiveData<Boolean>().apply { value = false }
    val isHistory: LiveData<Boolean> get() = _isHistory

    val errorState: MutableLiveData<ErrorState> = MutableLiveData<ErrorState>().apply {
        value = ErrorState.NONE
    }

    private val _isInputFocused = MutableLiveData<Boolean>()
    val isInputFocused: LiveData<Boolean> get() = _isInputFocused

    private val _isClearIconVisible = MutableLiveData<Boolean>()
    val isClearIconVisible: LiveData<Boolean> get() = _isClearIconVisible

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    private var isEditing = false

    val displayTrackList: LiveData<List<Track>> = MediatorLiveData<List<Track>>().also { mediator ->
        mediator.addSource(_isHistory) { isHistory ->
            mediator.value = if (isHistory) _trackHistoryList.value else _trackList.value ?: emptyList()
        }
        mediator.addSource(_trackList) {
            if (_isHistory.value == false) {
                mediator.value = _trackList.value ?: emptyList()
            }
        }
        mediator.addSource(_trackHistoryList) {
            if (_isHistory.value == true) {
                mediator.value = it
            }
        }
    }

    private val listener = SharedPreferences.OnSharedPreferenceChangeListener { prefs, key -> // 📦
        if (key == SearchHistory.TRACK_HISTORY_LIST_KEY) {
            val json = prefs.getString(SearchHistory.TRACK_HISTORY_LIST_KEY, "")
            val updatedList = if (!json.isNullOrEmpty())
                Gson().fromJson(json, Array<Track>::class.java).toList()
            else emptyList()

            _trackHistoryList.value = updatedList
            if (updatedList.isEmpty()) _isHistory.value = false
        }
    }

    init { // 👉
        sharedPreferences.registerOnSharedPreferenceChangeListener(listener)
        _trackHistoryList.value = searchHistory.getHistory()
    }

    override fun onCleared() {
        super.onCleared()
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(listener)
    }

    fun clearSearchInput() {
        _searchQuery.value = ""
        _isClearIconVisible.value = false
        clearTracks()
        errorState.value = ErrorState.NONE
    }

    fun setInputFocused(isFocused: Boolean) {
        _isInputFocused.value = isFocused
        updateIsHistoryAndTrackList(_searchQuery.value, isFocused)
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
        if (isInputFocused.value == true) {
            if (query.isNotEmpty()) {
                if (!isEditing) {
                    _isHistory.value = false
                    _trackList.value = mutableListOf()
                }
            } else {
                isEditing = false
                updateIsHistoryAndTrackList()
            }
        }
    }

    private fun updateIsHistoryAndTrackList(
        query: String? = _searchQuery.value,
        isFocused: Boolean = _isInputFocused.value == true
    ) {
        val history = searchHistory.getHistory()
        _isHistory.value = isFocused && query.isNullOrEmpty() && history.isNotEmpty()
    }

    fun onSearchActionDone() {
        isEditing = true
        _searchQuery.value = _searchQuery.value?.trim()
        if (!_searchQuery.value.isNullOrEmpty()) {
            performSearch()
        }
    }

    fun onTrackClicked(track: Track) {
        searchHistory.addTrackToHistory(track)
        _trackHistoryList.value = searchHistory.getHistory()
    }

    fun removeTrack(track: Track) {
        if (_isHistory.value == true) {
            val current = searchHistory.getHistory().toMutableList()
            val removed = current.removeIf { it.trackId == track.trackId }
            if (removed) {
                searchHistory.saveHistory(current)
                _trackHistoryList.value = current // ✨
            }
        } else {
            val currentTracks = _trackList.value?.toMutableList() ?: return
            val position = currentTracks.indexOf(track)
            if (position >= 0) {
                currentTracks.removeAt(position)
                _trackList.value = currentTracks
            }
        }
    }

    private fun performSearch() {
        val query = _searchQuery.value?.trim() ?: return
        if (query.isNotEmpty()) {
            _isLoading.value = true  //🔄
            apiService.search(query).enqueue(object : Callback<SearchResponse> {
                override fun onResponse(call: Call<SearchResponse>, response: Response<SearchResponse>) {
                    _isLoading.value = false  //🔄
                    if (response.isSuccessful && response.body() != null) {
                        val results = response.body()!!.results
                        if (results.isNotEmpty()) {
                            _trackList.value = results.toMutableList()
                            errorState.value = ErrorState.NONE
                        } else {
                            errorState.value = ErrorState.ERROR
                        }
                    }
                }

                override fun onFailure(call: Call<SearchResponse>, t: Throwable) {
                    _isLoading.value = false  //🔄
                    errorState.value = ErrorState.FAILURE
                }
            })
        }
    }

    private fun clearTracks() {
        _trackList.value = mutableListOf()
    }
}
