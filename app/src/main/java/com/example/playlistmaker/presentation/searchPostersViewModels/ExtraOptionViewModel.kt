package com.example.playlistmaker.presentation.searchPostersViewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.playlistmaker.domain.api.player.AudioPlayerInteraction
import com.example.playlistmaker.domain.api.player.PlaybackState
import com.example.playlistmaker.domain.models.player.TrackListInputData
import com.example.playlistmaker.domain.models.search.Track


class ExtraOptionViewModel(
    private val audioPlayer: AudioPlayerInteraction
) : ViewModel() {

    private val _state = MutableLiveData(ExtraOptionViewState())
    val state: LiveData<ExtraOptionViewState> = _state

    private val currentState: ExtraOptionViewState
        get() = _state.value ?: ExtraOptionViewState()

    init {
        initAudioCallbacks()
    }

    private fun initAudioCallbacks() {
        audioPlayer.setOnTimeUpdateCallback { time ->
            updateState {
                it.copy(trackList = it.trackList.map { track ->
                    if (track.trackId == audioPlayer.currentTrackId) track.copy(playTime = time) else track
                })
            }
        }

        audioPlayer.setStateChangeCallback { newState ->
            val updatedTracks = currentState.trackList.map {
                if (it.trackId == audioPlayer.getValidTrackId()) {
                    when (newState) {
                        PlaybackState.PREPARING -> it.copy(isPlaying = false, playTime = "...")
                        PlaybackState.PREPARED -> it.copy(isPlaying = false)
                        PlaybackState.PLAYING -> it.copy(isPlaying = true)
                        PlaybackState.PAUSED -> it.copy(isPlaying = false)
                        else -> it.copy(isPlaying = false, playTime = "0:00")
                    }
                } else it.copy(isPlaying = false, playTime = "0:00")
            }
            updateState { it.copy(trackList = updatedTracks, playbackState = newState) }
        }
    }

    fun initializeWith(inputData: TrackListInputData) {
        updateState {
            it.copy(
                trackList = inputData.trackList,
                currentTrackIndex = inputData.initialIndex,
                isBottomNavVisible = inputData.trackList.isEmpty()
            )
        }
    }

    fun setCurrentTrackIndex(index: Int) {
        updateState { it.copy(currentTrackIndex = index) }
    }

    fun toggleIsHorizontal() {
        updateState { it.copy(isHorizontal = !it.isHorizontal) }
    }

    fun setScrollPosition(pos: Int) {
        updateState { it.copy(scrollPosition = pos) }
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

    fun getCurrentTrack(): Track? = currentState.trackList.getOrNull(currentState.currentTrackIndex)

    override fun onCleared() {
        super.onCleared()
        audioPlayer.clearCallbacks()
    }

    fun updateState(transform: (ExtraOptionViewState) -> ExtraOptionViewState) {
        _state.value = transform(currentState)
    }
}
