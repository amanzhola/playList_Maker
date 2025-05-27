package com.example.playlistmaker.presentation.searchPostersViewModels

import com.example.playlistmaker.domain.api.player.PlaybackState
import com.example.playlistmaker.domain.models.search.Track

data class ExtraOptionViewState(
    val trackList: List<Track> = emptyList(),
    val currentTrackIndex: Int = 0,
    val scrollPosition: Int = -1,
    val isHorizontal: Boolean = true,
    val isBottomNavVisible: Boolean = true,
    val playbackState: PlaybackState = PlaybackState.IDLE
)