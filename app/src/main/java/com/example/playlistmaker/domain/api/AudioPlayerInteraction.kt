package com.example.playlistmaker.domain.api

interface AudioPlayerInteraction {
    enum class PlaybackState {
        IDLE, PREPARING, PREPARED, PLAYING, PAUSED, STOPPED
    }

    val currentTrackId: Int
    val playbackState: PlaybackState

    fun setTrack(previewUrl: String, trackId: Int)
    fun pause()
    fun resume()
    fun stop()
    fun setOnTimeUpdateCallback(callback: (String) -> Unit)
    fun setStateChangeCallback(callback: (PlaybackState) -> Unit)
    fun isCurrentTrackPlaying(trackId: Int): Boolean
    fun clearCallbacks()
    fun getValidTrackId(): Int
}