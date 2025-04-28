package com.example.playlistmaker.launcher

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.playlistmaker.extraOption.AudioPlayer
import com.example.playlistmaker.search.Track
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class TrackPreviewViewModel : ViewModel() {

    private val audioPlayer =  AudioPlayer.getInstance()  // 🎧 🕒

    private val _trackList = MutableLiveData<List<Track>>(emptyList())
    val trackList: LiveData<List<Track>> get() = _trackList

    private val _currentTrackIndex = MutableLiveData<Int>(0)
    val currentTrackIndex: LiveData<Int> get() = _currentTrackIndex

    private val _isHorizontal = MutableLiveData<Boolean>(true)
    val isHorizontal: LiveData<Boolean> get() = _isHorizontal

    private val _playbackState = MutableLiveData<AudioPlayer.PlaybackState>(AudioPlayer.PlaybackState.IDLE)
    val playbackState: LiveData<AudioPlayer.PlaybackState> get() = _playbackState

    private val _scrollPosition = MutableLiveData<Int>(-1)
    val scrollPosition: LiveData<Int> get() = _scrollPosition

    init {
        initAudioCallbacks()
    }

    private fun initAudioCallbacks() {
        audioPlayer.setOnTimeUpdateCallback { time ->
            val trackId = audioPlayer.currentTrackId
            _trackList.postValue(_trackList.value?.map {
                if (it.trackId == trackId) it.copy(playTime = "🕒$time") else it
            })
        }

        audioPlayer.setStateChangeCallback { state ->
            _playbackState.postValue(state)

            val trackId = audioPlayer.getValidTrackId()
            _trackList.postValue(_trackList.value?.map {
                if (it.trackId == trackId) {
                    when (state) {
                        AudioPlayer.PlaybackState.PREPARING -> it.copy(isPlaying = false, playTime = "🕒...")
                        AudioPlayer.PlaybackState.PREPARED -> it.copy(isPlaying = false)
                        AudioPlayer.PlaybackState.PLAYING -> it.copy(isPlaying = true)
                        AudioPlayer.PlaybackState.PAUSED -> it.copy(isPlaying = false)
                        AudioPlayer.PlaybackState.STOPPED,
                        AudioPlayer.PlaybackState.IDLE -> it.copy(isPlaying = false, playTime = "🕒0:00")
                    }
                } else {
                    it.copy(isPlaying = false, playTime = "🕒0:00")
                }
            })
        }
    }

    fun setTrackList(json: String) {
        _trackList.value = deserializeTrackList(json)
    }

    fun setCurrentTrackIndex(index: Int) {
        _currentTrackIndex.value = index
    }

    fun toggleIsHorizontal() {
        _isHorizontal.value = _isHorizontal.value?.not() ?: true
    }

    private fun deserializeTrackList(json: String): List<Track> {
        val gson = Gson()
        val trackType = object : TypeToken<List<Track>>() {}.type
        return gson.fromJson(json, trackType)
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
            audioPlayer.playbackState == AudioPlayer.PlaybackState.PAUSED && track.trackId == audioPlayer.currentTrackId -> {
                audioPlayer.resume()
            }
            else -> {
                audioPlayer.setTrack(track.previewUrl, track.trackId)
            }
        }
    }

    fun stopAudioPlay() {
        audioPlayer.stopPlayback()
    }

    override fun onCleared() {
        super.onCleared()
        audioPlayer.clearCallbacks()
    }
}