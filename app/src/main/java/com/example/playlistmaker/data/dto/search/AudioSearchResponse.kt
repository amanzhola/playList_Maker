package com.example.playlistmaker.data.dto.search

data class AudioSearchResponse(
    val resultCount: Int,
    val results: MutableList<TrackDto>
)
