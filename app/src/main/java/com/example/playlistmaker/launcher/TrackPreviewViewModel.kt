package com.example.playlistmaker.launcher

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.playlistmaker.extraOption.AudioPlayer
import com.example.playlistmaker.search.Track
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class TrackPreviewViewModel : ViewModel() {
    val currentPlayTimes = mutableMapOf<Int, String>() // 🕒
    val audioPlayer = AudioPlayer() // 🎧
    private val _trackList = MutableLiveData<List<Track>>()
    val trackList: LiveData<List<Track>> get() = _trackList

    private var _currentTrackIndex = MutableLiveData<Int>()
    val currentTrackIndex: LiveData<Int> get() = _currentTrackIndex

    var scrollPosition = -1
    var isHorizontal = true

    fun setTrackList(json: String) {
        _trackList.value = deserializeTrackList(json)
    }

    fun setCurrentTrackIndex(index: Int) {
        _currentTrackIndex.value = index
    }

    private fun deserializeTrackList(json: String): List<Track> {
        val gson = Gson()
        val trackType = object : TypeToken<List<Track>>() {}.type
        return gson.fromJson(json, trackType)
    }
}