package com.example.playlistmaker.data.dto.share_album

// Shared DTO — что кладём в ZIP -> playlist.json
data class SharedTrackDto(
    val trackId: Long,                 // внешний/стабильный id; потом сконвертим в Int
    val trackName: String,
    val artistName: String,
    val trackTimeMillis: Long,
    val artworkUrl100: String? = null,
    val collectionName: String? = null,
    val releaseDate: String? = null,
    val primaryGenreName: String? = null,
    val country: String? = null,
    val previewUrl: String? = null,
    val isFavorite: Boolean? = null
)
