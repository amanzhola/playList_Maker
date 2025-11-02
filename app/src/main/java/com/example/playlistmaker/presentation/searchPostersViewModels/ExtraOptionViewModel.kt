package com.example.playlistmaker.presentation.searchPostersViewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.exoplayer.ExoPlayer
import com.example.playlistmaker.domain.api.player.AudioPlayerControl
import com.example.playlistmaker.domain.api.player.PlaybackState
import com.example.playlistmaker.domain.api.song_db.FavoriteTracksInteractor
import com.example.playlistmaker.domain.models.player.TrackListInputData
import com.example.playlistmaker.domain.models.playlist.Playlist
import com.example.playlistmaker.domain.models.search.Track
import com.example.playlistmaker.domain.usecases.playlist.AddTrackToPlaylistUseCase
import com.example.playlistmaker.domain.usecases.playlist.ObservePlaylistsUseCase
import com.example.playlistmaker.utils.NO_VIDEO_POSITION
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ExtraOptionViewModel( // 🖼️ Детальный экран (Аудиоплеер)
    private val favoriteTracksInteractor: FavoriteTracksInteractor,
    observePlaylists: ObservePlaylistsUseCase,
    private val addTrackToPlaylist: AddTrackToPlaylistUseCase,
    private val exoProvider: ExoPlayerProvider
) : ViewModel() {
    // ────────────────── Utube old mob ──────────────────
    data class AudioProgress(val positionMs: Long, val durationMs: Long, val bufferedMs: Long)

    @Volatile private var lastAudioProgress: AudioProgress? = null
    fun getAudioProgress(): AudioProgress? = lastAudioProgress

    fun seekTo(ms: Long) { control?.seekTo(ms) }

    // ────────────────── Utube ──────────────────

    private val _exo = MutableStateFlow<ExoPlayer?>(null)
        val exoFlow: StateFlow<ExoPlayer?> = _exo

        var exo: ExoPlayer?
            get() = _exo.value
            set(value) { _exo.value = value }

    private val _videoPos = MutableStateFlow(NO_VIDEO_POSITION)
    val videoPosFlow: StateFlow<Int> = _videoPos

    var videoPos: Int
        get() = _videoPos.value
        set(value) { _videoPos.value = value }

    fun ensurePlayer() {
        if (_exo.value == null) {
            _exo.value = exoProvider.create()
        }
    }

    fun releasePlayer() {
        _exo.value?.release()
        _exo.value = null
        videoPos = NO_VIDEO_POSITION
    }

    override fun onCleared() {
        // На всякий случай освободим, если забыли
        _exo.value?.release()
        _exo.value = null
        super.onCleared()
    }

    // ────────────────── Плейлисты (как было) ──────────────────
    val playlists: StateFlow<List<Playlist>> =
        observePlaylists().stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    sealed interface PlaylistEvent {
        data class Added(val playlistName: String) : PlaylistEvent
        data class AlreadyExists(val playlistName: String) : PlaylistEvent
        data class Error(val message: String) : PlaylistEvent
    }
    private val _playlistEvents = MutableSharedFlow<PlaylistEvent>()
    val playlistEvents: Flow<PlaylistEvent> = _playlistEvents

    // ────────────────── Экранное состояние (как было) ──────────────────
    private val _state = MutableStateFlow(ExtraOptionViewState()) // 📦 текущее состояние
    val state: StateFlow<ExtraOptionViewState> = _state.asStateFlow()

    // ────────────────── Связь с сервисом ──────────────────
    // [ADDED] Был флаг isObserving в старой VM для AudioPlayerInteraction.
    //         Он НУЖЕН и здесь — чтобы не подписываться на AudioPlayerControl дважды.
    private var isObserving: Boolean = false // 🛡 защита от повторного запуска подписок

    private var control: AudioPlayerControl? = null
    private var collectJobUi: Job? = null
    private var collectJobPlayback: Job? = null

    /**
     * [REPLACEMENT for startObservingAudioPlayer()]
     * [REMOVED → REPLACED] Старый метод startObservingAudioPlayer() больше не нужен,
     *                      т.к. подписки теперь ставятся при успешной привязке к сервису.
     */
    // 🚀 Запуск наблюдения за плеером
    fun setAudioPlayerControl(control: AudioPlayerControl) {
        // защита от повторной подписки
        if (isObserving) {
            // уже наблюдаем — просто обновим ссылку при необходимости
            this.control = control
            return
        }
        isObserving = true
        this.control = control

        // [KEPT (by semantics)] подписка на UI-состояние плеера (прогресс/кнопка)
        collectJobUi?.cancel()

        // ────────────────── audio time bar updated prev collectJobUi?.cancel() + lastAudioProgress ──────────────────
        collectJobUi = viewModelScope.launch {
            control.getPlayerState().collect { ui ->
                lastAudioProgress = AudioProgress(ui.positionMs, ui.durationMs, ui.bufferedMs)
                _state.update { st ->
                    val updated = st.trackList.map { t ->
                        if (t.trackId == control.currentTrackId) {
                            t.copy(isPlaying = ui.isPlaying, playTime = ui.progress)
                        } else t.copy(isPlaying = false)
                    }
                    st.copy(trackList = updated)
                }
            }
        }

        // [KEPT (by semantics)] подписка на PlaybackState — влияет на кнопку и логику уведомления
        collectJobPlayback?.cancel()
        collectJobPlayback = viewModelScope.launch {
            control.getPlaybackState().collect { newState ->
                _state.update { st -> st.copy(playbackState = newState) }
            }
        }
    }

    fun removeAudioPlayerControl() {
        collectJobUi?.cancel()
        collectJobPlayback?.cancel()
        collectJobUi = null
        collectJobPlayback = null
        control = null

        // [ADDED] Сбрасываем флаг — чтобы при возврате на экран заново подписаться.
        isObserving = false
    }

    // ────────────────── ЖЦ UI → управление уведомлением ──────────────────
    fun onUiWentBackground() {
        // [KEPT] Поведение по заданию: если играем и уходим в фон — показать уведомление
        if (_state.value.playbackState == PlaybackState.PLAYING) {
            control?.startForegroundNow()
        }
    }

    fun onUiCameToForeground() {
        // [KEPT] Вернулись — скрыть уведомление (и отменить)
        control?.stopForegroundNow(cancelNotification = true)
    }

    // ────────────────── Управление воспроизведением (эквивалент логике было) ──────────────────

    // ▶️ наблюдаем за состоянием плеера
    // ▶️ Управление воспроизведением
    fun audioPlay(track: Track) {
        val c = control ?: return
        when (_state.value.playbackState) {
            PlaybackState.PLAYING -> {
                if (c.currentTrackId == track.trackId) {
                    c.pausePlayer()
                } else {
                    c.setTrack(track.previewUrl, track.trackId, track.artistName, track.trackName)
                    c.startPlayer()
                }
            }
            PlaybackState.PAUSED, PlaybackState.PREPARED, PlaybackState.COMPLETED, PlaybackState.IDLE -> {
                if (c.currentTrackId != track.trackId) {
                    c.setTrack(track.previewUrl, track.trackId, track.artistName, track.trackName)
                }
                c.startPlayer()
            }
            PlaybackState.PREPARING, PlaybackState.ERROR, PlaybackState.STOPPED -> {
                // [KEPT] мягкий ignore / можно подсветить ошибку/дать retry
            }
        }
    }

    fun stopAudioPlay() {
        control?.stopPlayer()
    }

    // ────────────────── Избранное (как было) ──────────────────
    // [RESTORED] private var _favoritesSyncStarted = false — НУЖЕН, чтобы не запускать подписку повторно
    private var _favoritesSyncStarted: Boolean = false

    // Чтобы не было «гонки» (сначала пришёл фаворит-сет, а trackList ещё пуст):
    // будущем нужен combine двух потоков (списка и избранного).
    private var lastFavoriteIds: Set<Int> = emptySet()

    // 🚀 запускаем один раз, чтобы держать флаги в актуальном состоянии
    fun startFavoritesSyncIfNeeded() {
        if (_favoritesSyncStarted) return
        _favoritesSyncStarted = true

        viewModelScope.launch {
            favoriteTracksInteractor
                .getAllFavorites()
                .map { list -> list.map { it.trackId }.toSet() }
                .distinctUntilChanged()
                .collect { favoriteIds ->
                    lastFavoriteIds = favoriteIds
                    _state.update { st ->
                        st.copy(
                            trackList = st.trackList.map { t ->
                                t.copy(isFavorite = t.trackId in favoriteIds)
                            }
                        )
                    }
                }
        }
    }

    // ────────────────── ВОЗВРАЩЁННЫЕ МЕТОДЫ (для совместимости с UI) ──────────────────

    /** [RESTORED] Безопасная заглушка — UI может вызывать при открытии шторки */
    fun onOpenBottomSheet() {
        // Flow из Room отдает актуальные плейлисты; по желанию можно дернуть refresh в репозитории.
    }

    /** [RESTORED] Клик по сердечку: оптимистичное обновление + запись в БД */
    // ❤️ клик по сердечку (по id трека — если приходят события из списка) // ❤️ Логика для кнопки "лайк"
    fun onFavoriteClicked(trackId: Int) = viewModelScope.launch {
        val list = _state.value.trackList
        val idx = list.indexOfFirst { it.trackId == trackId }
        if (idx == -1) return@launch

        val old = list[idx]
        val toggled = old.copy(isFavorite = !old.isFavorite)

        // 1) Оптимистично правим UI (точечно)
        // 1) 🔴 Оптимистично обновляем UI
        if (idx == _state.value.currentTrackIndex) {
            updateCurrentTrack(toggled)
        } else {
            _state.update { st ->
                val copy = st.trackList.toMutableList()
                copy[idx] = toggled
                st.copy(trackList = copy)
            }
        }

        // 2) Пишем в БД — подписка на избранное сама «подтвердит»/исправит
        // 💾 2) записываем в БД — поток из п.1 сам «подтвердит» итог
        try {
            if (toggled.isFavorite) {
                favoriteTracksInteractor.addToFavorites(toggled)
            } else {
                favoriteTracksInteractor.removeFromFavorites(old)
            }
        } catch (_: Exception) {
            // при желании можно локально откатить
        }
    }

    /** [RESTORED] Точечная замена текущего элемента в списке (используется в onFavoriteClicked) */
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

    // ────────────────── Инициализация и остальное (как было) ──────────────────

    // 🎯 Инициализация трек-листа
    fun initializeWith(inputData: TrackListInputData) {
        _state.update {
            it.copy(
                trackList = inputData.trackList.map { t ->
                    t.copy(isFavorite = t.trackId in lastFavoriteIds)
                },
                currentTrackIndex = inputData.initialIndex,
                isBottomNavVisible = inputData.trackList.isEmpty()
            )
        }
    }

    fun onPlaylistClicked(playlist: Playlist) {
        val track = getCurrentTrack() ?: return
        viewModelScope.launch {
            try {
                if (track.trackId in playlist.trackIds) {
                    _playlistEvents.emit(PlaylistEvent.AlreadyExists(playlist.name))
                } else {
                    val added = addTrackToPlaylist(playlist, track)
                    if (added) _playlistEvents.emit(PlaylistEvent.Added(playlist.name))
                    else _playlistEvents.emit(PlaylistEvent.AlreadyExists(playlist.name))
                }
            } catch (e: Exception) {
                _playlistEvents.emit(PlaylistEvent.Error(e.message ?: "Ошибка добавления"))
            }
        }
    }

    fun setCurrentTrackIndex(index: Int) { _state.update { it.copy(currentTrackIndex = index) } }
    // 🔄 Переключение ориентации (гориз./вертик.)
    fun toggleIsHorizontal() { _state.update { it.copy(isHorizontal = !it.isHorizontal) } }
    // 💾 Сохраняем позицию скролла
    fun setScrollPosition(pos: Int) { _state.update { it.copy(scrollPosition = pos) } }

    // 📦 Получить текущий трек
    fun getCurrentTrack(): Track? =
        _state.value.trackList.getOrNull(_state.value.currentTrackIndex)

    fun updateState(transform: (ExtraOptionViewState) -> ExtraOptionViewState) {
        _state.update(transform)
    }
}
