package com.example.playlistmaker.domain.models.playlist

data class Playlist(
    val id: Long,
    val name: String,
    val description: String?,
    val coverPath: String?,   // абсолютный путь к приватному файлу
    val trackIds: List<Int>        // 👈 новое поле чтобы VM без похода в БД знала, есть ли трек в плейлисте
){
    val tracksCount: Int get() = trackIds.size
}
