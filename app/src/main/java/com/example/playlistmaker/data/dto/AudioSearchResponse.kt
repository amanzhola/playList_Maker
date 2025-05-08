package com.example.playlistmaker.data.dto

data class AudioSearchResponse(
    val resultCount: Int,
    val results: MutableList<TrackDto>
)

//import com.example.playlistmaker.domain.models.Track
//data class AudioSearchResponse(
//    val resultCount: Int,
//    val results: MutableList<Track>
//)
