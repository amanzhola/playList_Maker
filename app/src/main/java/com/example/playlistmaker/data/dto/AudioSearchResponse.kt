package com.example.playlistmaker.data.dto

data class AudioSearchResponse(
    val resultCount: Int,
    val results: MutableList<TrackDto>
)
