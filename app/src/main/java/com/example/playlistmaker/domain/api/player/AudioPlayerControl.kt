package com.example.playlistmaker.domain.api.player

import kotlinx.coroutines.flow.StateFlow

data class PlayerUiState(
    val isButtonEnabled: Boolean,
    val isPlaying: Boolean,
    val progress: String,   // "mm:ss"
    val buttonText: String  // "PLAY" / "PAUSE"
)

interface AudioPlayerControl {

    // Управление
    fun startPlayer()
    fun pausePlayer()
    fun stopPlayer()

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
