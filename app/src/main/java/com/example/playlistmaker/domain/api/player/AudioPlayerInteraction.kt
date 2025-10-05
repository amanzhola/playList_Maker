package com.example.playlistmaker.domain.api.player

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

// add COMPLETED, ERROR for service on Player [Сервис (Bound + Foreground)]
enum class PlaybackState {
    IDLE, PREPARING, PREPARED, PLAYING, PAUSED, STOPPED, COMPLETED, ERROR
}

interface AudioPlayerInteraction {

    var currentTrackId: Int
    var lastPlayedTrackId: Int

    val playbackState: StateFlow<PlaybackState>
    val playTime: Flow<String>

    fun setTrack(previewUrl: String, trackId: Int)
    fun pause()
    fun resume()
    fun stopPlayback()
    fun isPlaying(): Boolean
    fun isCurrentTrackPlaying(trackId: Int): Boolean
    fun getValidTrackId(): Int

}
