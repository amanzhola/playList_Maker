package com.example.playlistmaker.presentation.searchPostersViewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.playlistmaker.domain.api.player.AudioPlayerInteraction
import com.example.playlistmaker.domain.api.player.PlaybackState
import com.example.playlistmaker.domain.api.song_db.FavoriteTracksInteractor
import com.example.playlistmaker.domain.models.player.TrackListInputData
import com.example.playlistmaker.domain.models.playlist.Playlist
import com.example.playlistmaker.domain.models.search.Track
import com.example.playlistmaker.domain.usecases.playlist.AddTrackToPlaylistUseCase
import com.example.playlistmaker.domain.usecases.playlist.ObservePlaylistsUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ExtraOptionViewModel( // 🖼️ Детальный экран (Аудиоплеер)
    private val audioPlayer: AudioPlayerInteraction,
    private val favoriteTracksInteractor: FavoriteTracksInteractor, // ❤️ интерактор для избранного
    // 👇 НОВОЕ
    observePlaylists: ObservePlaylistsUseCase,
    private val addTrackToPlaylist: AddTrackToPlaylistUseCase
) : ViewModel() {

    // ───────────────────────── ПЛЕЙЛИСТЫ (НОВОЕ) ─────────────────────────
    val playlists: StateFlow<List<Playlist>> =
        observePlaylists()
            .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    sealed interface PlaylistEvent {
        data class Added(val playlistName: String) : PlaylistEvent
        data class AlreadyExists(val playlistName: String) : PlaylistEvent
        data class Error(val message: String) : PlaylistEvent
    }
    private val _playlistEvents = MutableSharedFlow<PlaylistEvent>()
    val playlistEvents: Flow<PlaylistEvent> = _playlistEvents

    fun onOpenBottomSheet() {
        // Ничего не делаем: Flow из Room сам отдаёт актуальные данные
        // (можно триггернуть refresh в репозитории)
    }

    fun onPlaylistClicked(playlist: Playlist) {
        val track = getCurrentTrack() ?: return
        viewModelScope.launch {
            try {
                if (track.trackId in playlist.trackIds) {
                    _playlistEvents.emit(PlaylistEvent.AlreadyExists(playlist.name))
                } else {
                    val added = addTrackToPlaylist(playlist, track) // suspend
                    if (added) {
                        _playlistEvents.emit(PlaylistEvent.Added(playlist.name))
                    } else {
                        // на всякий случай, если репозиторий вернул false (гонка/повтор)
                        _playlistEvents.emit(PlaylistEvent.AlreadyExists(playlist.name))
                    }
                }
            } catch (e: Exception) {
                _playlistEvents.emit(PlaylistEvent.Error(e.message ?: "Ошибка добавления"))
            }
        }
    }
    // ─────────────────────── КОНЕЦ «плейлистовой» вставки ───────────────────────

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

    // Чтобы не было «гонки» (сначала пришёл фаворит-сет, а trackList ещё пуст):
    // будущем нужен combine двух потоков (списка и избранного).
    private var lastFavoriteIds: Set<Int> = emptySet()

    // 🚀 запускаем один раз, чтобы держать флаги в актуальном состоянии
    fun startFavoritesSyncIfNeeded() {
        if (_favoritesSyncStarted) return
        _favoritesSyncStarted = true

        viewModelScope.launch {
            favoriteTracksInteractor
                .getAllFavorites()                      // Flow<List<Track>>
                .map { list -> list.map { it.trackId }.toSet() } // → Set<Int>
                .distinctUntilChanged() // ✅ не спамим одинаковыми наборами
                .collect { favoriteIds ->

                    lastFavoriteIds = favoriteIds

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

    // 🎯 Инициализация трек-листа
    fun initializeWith(inputData: TrackListInputData) {
        _state.update { it ->
            it.copy(
                trackList = inputData.trackList.map { it.copy(isFavorite = it.trackId in lastFavoriteIds) },
                currentTrackIndex = inputData.initialIndex,
                isBottomNavVisible = inputData.trackList.isEmpty()
            )
        }
    }

    private var _favoritesSyncStarted = false

    // ❤️ клик по сердечку (по id трека — если приходят события из списка) // ❤️ Логика для кнопки "лайк"
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
