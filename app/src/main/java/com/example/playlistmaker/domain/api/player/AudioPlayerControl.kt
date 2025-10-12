package com.example.playlistmaker.domain.api.player

import kotlinx.coroutines.flow.StateFlow

data class PlayerUiState(
    val isButtonEnabled: Boolean,
    val isPlaying: Boolean,
    val progress: String,   // "mm:ss"
    val buttonText: String,  // "PLAY" / "PAUSE"

    // 🆕 миллисекунды для таймбара
    val positionMs: Long = 0L,
    val durationMs: Long = 0L,
    val bufferedMs: Long = 0L
)

interface AudioPlayerControl {

    // Управление
    fun startPlayer()
    fun pausePlayer()
    fun stopPlayer()

    // 🆕 for audio time bar
    fun seekTo(positionMs: Long)

    // Трек и метаданные для нотификации
    fun setTrack(
        url: String,
        trackId: Int,
        artist: String,
        title: String
    )

    // Наблюдение
    fun getPlayerState(): StateFlow<PlayerUiState>
    fun getPlaybackState(): StateFlow<PlaybackState>

    // Foreground
    fun startForegroundNow()
    fun stopForegroundNow(cancelNotification: Boolean)

    // Текущее
    val currentTrackId: Int?
}
