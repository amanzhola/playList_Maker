package com.example.playlistmaker.presentation.media

import com.example.playlistmaker.domain.models.search.Track

data class FavoriteTracksState(
    val isEmpty: Boolean = true,
    val tracks: List<Track> = emptyList()
)
