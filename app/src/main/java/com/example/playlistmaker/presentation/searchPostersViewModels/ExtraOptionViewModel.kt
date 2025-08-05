package com.example.playlistmaker.presentation.searchPostersViewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.playlistmaker.domain.api.player.AudioPlayerInteraction
import com.example.playlistmaker.domain.api.player.PlaybackState
import com.example.playlistmaker.domain.api.song_db.FavoriteTracksInteractor
import com.example.playlistmaker.domain.models.player.TrackListInputData
import com.example.playlistmaker.domain.models.search.Track
import kotlinx.coroutines.launch

class ExtraOptionViewModel( // 🖼️ Детальный экран (Аудиоплеер)
    private val audioPlayer: AudioPlayerInteraction,
    private val favoriteTracksInteractor: FavoriteTracksInteractor // 🆕 интерактор для избранного
) : ViewModel() {

    private val _state = MutableLiveData(ExtraOptionViewState())
    val state: LiveData<ExtraOptionViewState> = _state

    private val currentState: ExtraOptionViewState
        get() = _state.value ?: ExtraOptionViewState()

    init {
        observeAudioPlayer() // 🔄 наблюдаем за плеером
    }

    private fun observeAudioPlayer() {
        // ⏱️ наблюдаем за временем проигрывания
        viewModelScope.launch {
            audioPlayer.playTime.collect { time ->
                updateState {
                    it.copy(trackList = it.trackList.map { track ->
                        if (track.trackId == audioPlayer.currentTrackId)
                            track.copy(playTime = time)
                        else track
                    })
                }
            }
        }

        // ▶️ наблюдаем за состоянием плеера
        viewModelScope.launch {
            audioPlayer.playbackState.collect { newState ->
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

    // 🆕 Логика для кнопки "лайк"
    fun onFavoriteClicked() {
        val track = getCurrentTrack() ?: return
        viewModelScope.launch {
            if (track.isFavorite) {
                favoriteTracksInteractor.removeFromFavorites(track)
            } else {
                favoriteTracksInteractor.addToFavorites(track)
            }
            track.isFavorite = !track.isFavorite // переключаем флаг
            updateCurrentTrack(track) // обновляем состояние списка
        }
    }

    private fun updateCurrentTrack(updatedTrack: Track) {
        updateState {
            val updatedList = it.trackList.toMutableList()
            if (it.currentTrackIndex in updatedList.indices) {
                updatedList[it.currentTrackIndex] = updatedTrack
            }
            it.copy(trackList = updatedList)
        }
    }

    fun setCurrentTrackIndex(index: Int) {
        updateState { it.copy(currentTrackIndex = index) }
    }

    // 🔄 Переключаем режим просмотра (горизонтальный / вертикальный)
    fun toggleIsHorizontal() {
        updateState { it.copy(isHorizontal = !it.isHorizontal) }
    }

    // 💾 Сохраняем позицию скролла
    fun setScrollPosition(pos: Int) {
        updateState { it.copy(scrollPosition = pos) }
    }

    // ▶️ Управляем воспроизведением
    fun audioPlay(track: Track) {
        when {
            audioPlayer.isCurrentTrackPlaying(track.trackId) -> audioPlayer.pause()
            audioPlayer.playbackState.value == PlaybackState.PAUSED &&
                    track.trackId == audioPlayer.currentTrackId -> audioPlayer.resume()
            else -> audioPlayer.setTrack(track.previewUrl, track.trackId)
        }
    }

    fun stopAudioPlay() {
        audioPlayer.stopPlayback()
    }

    fun getCurrentTrack(): Track? =
        currentState.trackList.getOrNull(currentState.currentTrackIndex)

    fun updateState(transform: (ExtraOptionViewState) -> ExtraOptionViewState) {
        _state.value = transform(currentState)
    }
}
