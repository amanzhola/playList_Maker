package com.example.playlistmaker.presentation.launcherViewModels

import com.example.playlistmaker.domain.api.player.PlaybackState
import com.example.playlistmaker.domain.models.search.Track

data class TrackPreviewViewState(
    val trackList: List<Track> = emptyList(),
    val currentTrackIndex: Int = 0,
    val isHorizontal: Boolean = true,
    val playbackState: PlaybackState = PlaybackState.IDLE,
    val scrollPosition: Int = -1
)
