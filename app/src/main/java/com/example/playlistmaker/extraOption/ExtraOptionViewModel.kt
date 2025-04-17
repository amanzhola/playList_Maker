package com.example.playlistmaker

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.playlistmaker.extraOption.AudioPlayer
import com.example.playlistmaker.search.Track
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class ExtraOptionViewModel : ViewModel() {

    val audioPlayer = AudioPlayer() // 🎧

    private val _trackList = MutableLiveData<List<Track>>(emptyList())
    val trackList: LiveData<List<Track>> get() = _trackList

    private val _currentTrackIndex = MutableLiveData<Int>(0)
    val currentTrackIndex: LiveData<Int> get() = _currentTrackIndex

    var isBottomNavVisible: Boolean = true
    var scrollPosition: Int = -1
    var isHorizontal: Boolean = true

    fun setTrackList(json: String) {
        val type = object : TypeToken<List<Track>>() {}.type
        _trackList.value = Gson().fromJson(json, type) ?: emptyList()
    }

    fun setCurrentTrackIndex(index: Int) {
        _currentTrackIndex.value = index
    }

    fun toggleBottomNavVisibility() {
        isBottomNavVisible = !isBottomNavVisible
    }
}