package com.example.playlistmaker.data.mappers

import com.example.playlistmaker.data.dto.share_album.SharedTrackDto
import com.example.playlistmaker.domain.models.search.Track

// TrackMappers.kt
fun Track.toShared() = SharedTrackDto(
    trackId = trackId.toLong(),
    trackName = trackName,
    artistName = artistName,
    trackTimeMillis = trackTimeMillis,
    artworkUrl100 = artworkUrl100,
    collectionName = collectionName,
    releaseDate = releaseDate,
    primaryGenreName = primaryGenreName,
    country = country,
    previewUrl = previewUrl,
    isFavorite = isFavorite
)

fun SharedTrackDto.toDomain() =
    Track(
        trackName = trackName,
        artistName = artistName,
        trackTimeMillis = trackTimeMillis,
        artworkUrl100 = artworkUrl100,
        collectionName = collectionName.orEmpty(),
        releaseDate = releaseDate.orEmpty(),
        primaryGenreName = primaryGenreName.orEmpty(),
        country = country.orEmpty(),
        previewUrl = previewUrl.orEmpty(),
        trackId = trackId.toInt(),
        isPlaying = false,
        playTime = "0:00",
        isFavorite = isFavorite ?: false
    )