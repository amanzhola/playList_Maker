package com.example.playlistmaker.presentation.launcherViewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.playlistmaker.domain.api.player.AudioPlayerInteraction
import com.example.playlistmaker.domain.api.player.PlaybackState
import com.example.playlistmaker.domain.models.search.Track
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch

class TrackPreviewViewModel(
    private val audioPlayer: AudioPlayerInteraction
) : ViewModel() {

    private val _state = MutableLiveData(TrackPreviewViewState())
    val state: LiveData<TrackPreviewViewState> get() = _state

    private val currentState: TrackPreviewViewState
        get() = _state.value ?: TrackPreviewViewState()

    init {
        observeAudioPlayer()
    }

    private fun observeAudioPlayer() {
        viewModelScope.launch {
            combine(
                audioPlayer.playbackState,
                audioPlayer.playTime.onStart { emit("0:00") }
            ) { state, time -> state to time }
                .collect { (newState, time) ->
                    updateTrackListByState(newState, time)
                }
        }
    }

    private fun updateTrackListByState(playbackState: PlaybackState, time: String) {
        val trackId = audioPlayer.getValidTrackId()

        val updatedTracks = currentState.trackList.map {
            if (it.trackId == trackId) {
                val playTime = "🕒$time"
                when (playbackState) {
                    PlaybackState.PREPARING -> it.copy(isPlaying = false, playTime = "🕒...")
                    PlaybackState.PREPARED -> it.copy(isPlaying = false, playTime = "🕒0:00")
                    PlaybackState.PLAYING -> it.copy(isPlaying = true, playTime = playTime)
                    PlaybackState.PAUSED -> it.copy(isPlaying = false, playTime = playTime)
                    PlaybackState.STOPPED, PlaybackState.IDLE -> it.copy(isPlaying = false, playTime = "🕒0:00")
                }
            } else {
                it.copy(isPlaying = false, playTime = "🕒0:00")
            }
        }

        updateState {
            it.copy(
                playbackState = playbackState,
                trackList = updatedTracks
            )
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

    fun audioPlay(track: Track) {
        when {
            audioPlayer.isCurrentTrackPlaying(track.trackId) -> audioPlayer.pause()
            audioPlayer.playbackState.value == PlaybackState.PAUSED && track.trackId == audioPlayer.currentTrackId -> audioPlayer.resume()
            else -> audioPlayer.setTrack(track.previewUrl, track.trackId)
        }
    }

    fun stopAudioPlay() {
        audioPlayer.stopPlayback()
    }

    private fun updateState(transform: (TrackPreviewViewState) -> TrackPreviewViewState) {
        _state.value = transform(currentState)
    }
}
