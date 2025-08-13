package com.example.playlistmaker.presentation.media

import com.example.playlistmaker.domain.models.search.Track

data class FavoriteTracksState(
    val isLoading: Boolean = true,
    val tracks: List<Track> = emptyList()
) {
    val isEmpty: Boolean get() = !isLoading && tracks.isEmpty()
}
