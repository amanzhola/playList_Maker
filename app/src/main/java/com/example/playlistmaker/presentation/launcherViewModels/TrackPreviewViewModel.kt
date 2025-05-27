package com.example.playlistmaker.presentation.launcherViewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.playlistmaker.domain.api.player.AudioPlayerInteraction
import com.example.playlistmaker.domain.api.player.PlaybackState
import com.example.playlistmaker.domain.models.search.Track

class TrackPreviewViewModel(
    private val audioPlayer: AudioPlayerInteraction
) : ViewModel() {

    private val _state = MutableLiveData(TrackPreviewViewState())
    val state: LiveData<TrackPreviewViewState> get() = _state

    private val currentState: TrackPreviewViewState
        get() = _state.value ?: TrackPreviewViewState()

    init {
        initAudioCallbacks()
    }

    private fun initAudioCallbacks() {
        audioPlayer.setOnTimeUpdateCallback { time ->
            val updatedTracks = currentState.trackList.map {
                if (it.trackId == audioPlayer.currentTrackId) it.copy(playTime = "🕒$time") else it
            }
            updateState { it.copy(trackList = updatedTracks) }
        }

        audioPlayer.setStateChangeCallback { newState ->
            val trackId = audioPlayer.getValidTrackId()
            val updatedTracks = currentState.trackList.map {
                if (it.trackId == trackId) {
                    when (newState) {
                        PlaybackState.PREPARING -> it.copy(isPlaying = false, playTime = "🕒...")
                        PlaybackState.PREPARED -> it.copy(isPlaying = false)
                        PlaybackState.PLAYING -> it.copy(isPlaying = true)
                        PlaybackState.PAUSED -> it.copy(isPlaying = false)
                        PlaybackState.STOPPED,
                        PlaybackState.IDLE -> it.copy(isPlaying = false, playTime = "🕒0:00")
                    }
                } else {
                    it.copy(isPlaying = false, playTime = "🕒0:00")
                }
            }
            updateState {
                it.copy(
                    playbackState = newState,
                    trackList = updatedTracks
                )
            }
        }
    }

    fun initialize(tracks: List<Track>, index: Int) {
        updateState {
            it.copy(trackList = tracks, currentTrackIndex = index)
        }
    }

    fun setCurrentTrackIndex(index: Int) {
        updateState { it.copy(currentTrackIndex = index) }
    }

    fun toggleIsHorizontal() {
        updateState { it.copy(isHorizontal = !it.isHorizontal) }
    }

    fun setScrollPosition(position: Int) {
        updateState { it.copy(scrollPosition = position) }
    }

    fun clearScrollPosition() {
        updateState { it.copy(scrollPosition = -1) }
    }

    fun audioPlay(track: Track) {
        when {
            audioPlayer.isCurrentTrackPlaying(track.trackId) -> audioPlayer.pause()
            audioPlayer.playbackState == PlaybackState.PAUSED && track.trackId == audioPlayer.currentTrackId -> audioPlayer.resume()
            else -> audioPlayer.setTrack(track.previewUrl, track.trackId)
        }
    }

    fun stopAudioPlay() {
        audioPlayer.stopPlayback()
    }

    fun getCurrentTrack(): Track? {
        val list = currentState.trackList
        val index = currentState.currentTrackIndex
        return list.getOrNull(index)
    }

    override fun onCleared() {
        super.onCleared()
        audioPlayer.clearCallbacks()
    }

    private fun updateState(transform: (TrackPreviewViewState) -> TrackPreviewViewState) {
        _state.value = transform(currentState)
    }
}
