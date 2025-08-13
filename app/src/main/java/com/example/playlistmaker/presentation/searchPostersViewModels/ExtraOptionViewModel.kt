package com.example.playlistmaker.presentation.searchPostersViewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.playlistmaker.domain.api.player.AudioPlayerInteraction
import com.example.playlistmaker.domain.api.player.PlaybackState
import com.example.playlistmaker.domain.api.song_db.FavoriteTracksInteractor
import com.example.playlistmaker.domain.models.player.TrackListInputData
import com.example.playlistmaker.domain.models.search.Track
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ExtraOptionViewModel( // 🖼️ Детальный экран (Аудиоплеер)
    private val audioPlayer: AudioPlayerInteraction,
    private val favoriteTracksInteractor: FavoriteTracksInteractor // ❤️ интерактор для избранного
) : ViewModel() {

    private val _state = MutableStateFlow(ExtraOptionViewState()) // 📦 текущее состояние
    val state: StateFlow<ExtraOptionViewState> = _state.asStateFlow()

    private var isObserving = false // 🛡 защита от повторного запуска подписок

    // 🚀 Запуск наблюдения за плеером
    fun startObservingAudioPlayer() {
        if (isObserving) return
        isObserving = true

        // ⏱️ наблюдаем за временем проигрывания
        viewModelScope.launch {
            audioPlayer.playTime.collect { time ->
                _state.update { state ->
                    state.copy(trackList = state.trackList.map { track ->
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
                _state.update { state ->
                    val updatedTracks = state.trackList.map { track ->
                        if (track.trackId == audioPlayer.getValidTrackId()) {
                            when (newState) {
                                PlaybackState.PREPARING -> track.copy(isPlaying = false, playTime = "…")
                                PlaybackState.PREPARED  -> track.copy(isPlaying = false)
                                PlaybackState.PLAYING   -> track.copy(isPlaying = true)
                                PlaybackState.PAUSED    -> track.copy(isPlaying = false)
                                else                    -> track.copy(isPlaying = false, playTime = "0:00")
                            }
                        } else track.copy(isPlaying = false, playTime = "0:00")
                    }
                    state.copy(trackList = updatedTracks, playbackState = newState)
                }
            }
        }
    }

    // 🎯 Инициализация трек-листа
    fun initializeWith(inputData: TrackListInputData) {
        _state.update {
            it.copy(
                trackList = inputData.trackList,
                currentTrackIndex = inputData.initialIndex,
                isBottomNavVisible = inputData.trackList.isEmpty()
            )
        }
    }

    // ❤️ Логика для кнопки "лайк"
    fun onFavoriteClicked() {
        val track = getCurrentTrack() ?: return
        viewModelScope.launch {
            if (track.isFavorite) {
                favoriteTracksInteractor.removeFromFavorites(track) // ❌ убираем
            } else {
                favoriteTracksInteractor.addToFavorites(track) // ➕ добавляем
            }
            track.isFavorite = !track.isFavorite // 🔄 переключаем флаг
            updateCurrentTrack(track) // 🆙 обновляем в списке
        }
    }

    // 🔄 Обновление текущего трека в списке
    private fun updateCurrentTrack(updatedTrack: Track) {
        _state.update { state ->
            val updatedList = state.trackList.toMutableList()
            if (state.currentTrackIndex in updatedList.indices) {
                updatedList[state.currentTrackIndex] = updatedTrack
            }
            state.copy(trackList = updatedList)
        }
    }

    fun setCurrentTrackIndex(index: Int) {
        _state.update { it.copy(currentTrackIndex = index) }
    }

    // 🔄 Переключение ориентации (гориз./вертик.)
    fun toggleIsHorizontal() {
        _state.update { it.copy(isHorizontal = !it.isHorizontal) }
    }

    // 💾 Сохраняем позицию скролла
    fun setScrollPosition(pos: Int) {
        _state.update { it.copy(scrollPosition = pos) }
    }

    // ▶️ Управление воспроизведением
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

    // 📦 Получить текущий трек
    fun getCurrentTrack(): Track? =
        _state.value.trackList.getOrNull(_state.value.currentTrackIndex)

    fun updateState(transform: (ExtraOptionViewState) -> ExtraOptionViewState) {
        _state.update(transform)
    }
}
