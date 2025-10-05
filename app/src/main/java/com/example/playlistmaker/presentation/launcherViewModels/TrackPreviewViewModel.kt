package com.example.playlistmaker.presentation.launcherViewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.playlistmaker.domain.api.player.AudioPlayerInteraction
import com.example.playlistmaker.domain.api.player.PlaybackState
import com.example.playlistmaker.domain.api.song_db.FavoriteTracksInteractor
import com.example.playlistmaker.domain.models.playlist.Playlist
import com.example.playlistmaker.domain.models.search.Track
import com.example.playlistmaker.domain.usecases.playlist.AddTrackToPlaylistUseCase
import com.example.playlistmaker.domain.usecases.playlist.ObservePlaylistsUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TrackPreviewViewModel(
    private val audioPlayer: AudioPlayerInteraction,
    // 👇 НОВОЕ
    observePlaylists: ObservePlaylistsUseCase,
    private val addTrackToPlaylist: AddTrackToPlaylistUseCase,
    // ★ NEW: интерактор избранного
    private val favoriteTracksInteractor: FavoriteTracksInteractor
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

    // ───── Избранное (NEW) ───── // ❤️ Логика для кнопки "лайк"
    private var favoritesSyncStarted = false
    private var lastFavoriteIds: Set<Int> = emptySet()

    /** Запусти 1 раз из UI (onCreate/onViewCreated): держим isFavorite в актуальном состоянии */
    fun startFavoritesSyncIfNeeded() {
        if (favoritesSyncStarted) return
        favoritesSyncStarted = true

        viewModelScope.launch {
            favoriteTracksInteractor
                .getAllFavorites()                       // Flow<List<Track>>
                .map { list -> list.map { it.trackId }.toSet() } // → Set<Int>
                .distinctUntilChanged()
                .collect { favIds ->
                    lastFavoriteIds = favIds
                    updateState { st ->
                        st.copy(
                            trackList = st.trackList.map { t ->
                                // НЕ трогаем playTime/isPlaying
                                t.copy(isFavorite = t.trackId in favIds)
                            }
                        )
                    }
                }
        }
    }

    /** Клик по сердечку конкретного трека */
    fun onFavoriteClicked(trackId: Int) = viewModelScope.launch {
        val list = currentState.trackList
        val idx = list.indexOfFirst { it.trackId == trackId }
        if (idx == -1) return@launch

        val old = list[idx]
        val toggled = old.copy(isFavorite = !old.isFavorite)

        // 1) Оптимистично обновляем UI
        updateState { st ->
            val copy = st.trackList.toMutableList()
            copy[idx] = toggled
            st.copy(trackList = copy)
        }

        // 2) Пишем в БД — поток из startFavoritesSyncIfNeeded() сам подтвердит/исправит
        try {
            if (toggled.isFavorite) {
                favoriteTracksInteractor.addToFavorites(toggled)
            } else {
                favoriteTracksInteractor.removeFromFavorites(old)
            }
        } catch (_: Exception) {
            // по желанию можно откатить локально
        }
    }

    // ───── Остальная логика плеера — как было ───── // ❤️ Логика для кнопки "лайк"


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
                    // fix instead false to true for isPlaying on PREPARING AND PREPARET
                    PlaybackState.PREPARING -> it.copy(isPlaying = true, playTime = "🕒...")
                    PlaybackState.PREPARED -> it.copy(isPlaying = true, playTime = "🕒0:00")
                    PlaybackState.PLAYING -> it.copy(isPlaying = true, playTime = playTime)
                    PlaybackState.PAUSED -> it.copy(isPlaying = false, playTime = playTime)
                    PlaybackState.STOPPED, PlaybackState.IDLE, PlaybackState.COMPLETED,
                    PlaybackState.ERROR -> it.copy(isPlaying = false, playTime = "🕒0:00")
                    // НОВОЕ: обнуляемся как при STOPPED/IDLE update AudioPlayerInteraction PlaybackState + for adding service on Player
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
            // при инициализации можно сразу расставить флаги из lastFavoriteIds
            it.copy(
                trackList = tracks.map { t -> t.copy(isFavorite = t.trackId in lastFavoriteIds) },
                currentTrackIndex = index
            )
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

    /** Текущий трек (для добавления в плейлист, шаринга и т.п.) */
    fun getCurrentTrack(): Track? {
        val st = currentState
        if (st.trackList.isEmpty()) return null

        // 1) пробуем по id из плеера (если setTrack уже вызывался)
        val playingId = audioPlayer.getValidTrackId() // уже используется выше
        st.trackList.firstOrNull { it.trackId == playingId }?.let { return it }

        // 2) fallback — по индексу из состояния
        val idx = st.currentTrackIndex
        return if (idx in st.trackList.indices) st.trackList[idx] else null
    }

}
