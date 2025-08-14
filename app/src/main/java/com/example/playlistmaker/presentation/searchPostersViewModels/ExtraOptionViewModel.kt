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
import kotlinx.coroutines.flow.map
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

    // 🚀 запускаем один раз, чтобы держать флаги в актуальном состоянии
    fun startFavoritesSyncIfNeeded() {
        if (_favoritesSyncStarted) return
        _favoritesSyncStarted = true

        viewModelScope.launch {
            favoriteTracksInteractor
                .getAllFavorites()                      // Flow<List<Track>>
                .map { list -> list.map { it.trackId }.toSet() } // → Set<Int>
                .collect { favoriteIds ->
                    // 💾 любой апдейт в БД → сразу отражаем в экране
                    _state.update { st ->
                        st.copy(
                            trackList = st.trackList.map { t ->
                                // НЕ трогаем прочие поля (🕒 playTime / ▶ isPlaying)
                                t.copy(isFavorite = t.trackId in favoriteIds)
                            }
                        )
                    }
                }
        }
    }
    private var _favoritesSyncStarted = false

    // ❤️ клик по сердечку (без параметров) — на текущем треке
    fun onFavoriteClicked() { // ❤️ Логика для кнопки "лайк"
        getCurrentTrack()?.let { onFavoriteClicked(it.trackId) }
    }

    // ❤️ клик по сердечку (по id трека — если приходят события из списка)
    fun onFavoriteClicked(trackId: Int) = viewModelScope.launch {
        val list = _state.value.trackList
        val idx = list.indexOfFirst { it.trackId == trackId }
        if (idx == -1) return@launch

        val old = list[idx]
        val toggled = old.copy(isFavorite = !old.isFavorite)

        // 1) 🔴 Оптимистично обновляем UI
        if (idx == _state.value.currentTrackIndex) {
            // 👉 для текущего трека сохраняем прежнюю логику
            updateCurrentTrack(toggled)
        } else {
            // 👉 для остальных — точечная замена по индексу
            _state.update { st ->
                val copy = st.trackList.toMutableList()
                copy[idx] = toggled
                st.copy(trackList = copy)
            }
        }

        // 💾 2) записываем в БД — поток из п.1 сам «подтвердит» итог
        try {
            if (toggled.isFavorite) { // 🔄 переключаем флаг
                favoriteTracksInteractor.addToFavorites(toggled) // ➕ добавляем
            } else {
                favoriteTracksInteractor.removeFromFavorites(old) // ❌ убираем
            }
        } catch (_: Exception) {
            // при редком фейле можно откатить локально,
            // но часто достаточно дождаться эмиссии из БД
        }
    }

    // 🔄 Обновление текущего трека в списке // 🆙 обновляем в списке
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
