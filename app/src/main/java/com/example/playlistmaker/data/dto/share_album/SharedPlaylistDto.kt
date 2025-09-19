package com.example.playlistmaker.data.dto.share_album

data class SharedPlaylistDto(
    val name: String,
    val description: String?,
    val coverFileName: String?,          // "cover.jpg" внутри ZIP
    val tracks: List<SharedTrackDto>
)