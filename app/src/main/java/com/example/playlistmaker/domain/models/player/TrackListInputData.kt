package com.example.playlistmaker.domain.models.player

import com.example.playlistmaker.domain.models.search.Track

data class TrackListInputData(
    val trackList: List<Track>,
    val initialIndex: Int
)
