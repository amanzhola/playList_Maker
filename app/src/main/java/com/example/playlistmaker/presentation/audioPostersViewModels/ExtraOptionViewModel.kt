package com.example.playlistmaker.presentation.audioPostersViewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.playlistmaker.domain.api.AudioPlayerInteraction
import com.example.playlistmaker.domain.api.PlaybackState
import com.example.playlistmaker.domain.models.Track
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class ExtraOptionViewModel(
    private val audioPlayer: AudioPlayerInteraction // 🎧
) : ViewModel() {

    private val _trackList = MutableLiveData<List<Track>>(emptyList())
    val trackList: LiveData<List<Track>> get() = _trackList

    private val _currentTrackIndex = MutableLiveData<Int>(0)
    val currentTrackIndex: LiveData<Int> get() = _currentTrackIndex

    private val _isHorizontal = MutableLiveData<Boolean>(true)
    val isHorizontal: LiveData<Boolean> get() = _isHorizontal

    private val _playbackState = MutableLiveData<PlaybackState>(PlaybackState.IDLE)
    val playbackState: LiveData<PlaybackState> get() = _playbackState

    var isBottomNavVisible: Boolean = true

    private val _scrollPosition = MutableLiveData<Int>(-1)
    val scrollPosition: LiveData<Int> get() = _scrollPosition

    init {
        initAudioCallbacks()
    }

    private fun initAudioCallbacks() {
        audioPlayer.setOnTimeUpdateCallback { time ->
            val trackId = audioPlayer.currentTrackId
            _trackList.postValue(_trackList.value?.map { // 🚑
//                if (it.trackId == trackId) it.copy(playTime = "🕒$time") else it
                if (it.trackId == trackId) it.copy(playTime = time) else it // ☝️ ❌ 🔁 🧩 ❓
            })
        }

        audioPlayer.setStateChangeCallback { state ->
            _playbackState.postValue(state)

            val trackId = audioPlayer.getValidTrackId()
            _trackList.postValue(_trackList.value?.map { // 🚑
                if (it.trackId == trackId) {
                    when (state) { // 📚
//                        PlaybackState.PREPARING -> it.copy(isPlaying = false, playTime = "🕒...")
                        PlaybackState.PREPARING -> it.copy(isPlaying = false, playTime = "...") // ☝️ ❌ 🔁 🧩 ❓
                        PlaybackState.PREPARED -> it.copy(isPlaying = false)
                        PlaybackState.PLAYING -> it.copy(isPlaying = true)
                        PlaybackState.PAUSED -> it.copy(isPlaying = false)
                        PlaybackState.STOPPED,
//                        PlaybackState.IDLE -> it.copy(isPlaying = false, playTime = "🕒0:00")
                        PlaybackState.IDLE -> it.copy(isPlaying = false, playTime = "0:00")// ☝️ ❌ 🔁 🧩 ❓
                    }
                } else {
//                    it.copy(isPlaying = false, playTime = "🕒0:00")
                    it.copy(isPlaying = false, playTime = "0:00")// ☝️ ❌ 🔁 🧩 ❓
                }
            })
        }
    }

    fun setTrackList(json: String) {
        val type = object : TypeToken<List<Track>>() {}.type
        _trackList.value = Gson().fromJson(json, type) ?: emptyList()
    }

    fun setCurrentTrackIndex(index: Int) {
        _currentTrackIndex.value = index
    }

    fun toggleIsHorizontal() {
        _isHorizontal.value = _isHorizontal.value?.not() ?: true
    }

    fun toggleBottomNavVisibility() { // 🚗
        isBottomNavVisible = !isBottomNavVisible
    }

    fun setScrollPosition(position: Int) {
        _scrollPosition.value = position
    }

    fun clearScrollPosition() {
        _scrollPosition.value = -1
    }

    fun audioPlay(track: Track) {
        when {
            audioPlayer.isCurrentTrackPlaying(track.trackId) -> {
                audioPlayer.pause()
            }
            audioPlayer.playbackState == PlaybackState.PAUSED && track.trackId == audioPlayer.currentTrackId -> {
                audioPlayer.resume()
            }
            else -> {
                audioPlayer.setTrack(track.previewUrl, track.trackId)
            }
        }
    }

    fun stopAudioPlay() { // ⛔
        audioPlayer.stopPlayback()
    }

    override fun onCleared() {
        super.onCleared()
        audioPlayer.clearCallbacks()
    }

    fun getCurrentTrack(): Track? {
        val list = _trackList.value
        val index = _currentTrackIndex.value
        if (list != null && index != null && index in list.indices) {
            return list[index]
        }
        return null
    }
}