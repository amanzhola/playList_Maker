package com.example.playlistmaker.domain.api.player

enum class PlaybackState {
    IDLE, PREPARING, PREPARED, PLAYING, PAUSED, STOPPED
}

interface AudioPlayerInteraction {

    var currentTrackId: Int
    var lastPlayedTrackId: Int
    var playbackState: PlaybackState

    fun setOnTimeUpdateCallback(callback: (String) -> Unit)
    fun setStateChangeCallback(callback: (PlaybackState) -> Unit)
    fun setTrack(previewUrl: String, trackId: Int)
    fun pause()
    fun resume()
    fun stopPlayback()
    fun isPlaying(): Boolean
    fun isCurrentTrackPlaying(trackId: Int): Boolean
    fun getValidTrackId(): Int
    fun clearCallbacks()
}