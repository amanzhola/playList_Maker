package com.example.playlistmaker.data.mappers

import com.example.playlistmaker.data.createPlaylist.PlaylistEntity
import com.example.playlistmaker.data.playlist.PlaylistTrackEntity
import com.example.playlistmaker.domain.models.playlist.Playlist
import com.example.playlistmaker.domain.models.search.Track
import com.google.common.reflect.TypeToken
import com.google.gson.Gson

fun PlaylistEntity.toDomain(gson: Gson): Playlist =
    Playlist(
        id = id,
        name = name,
        description = description,
        coverPath = coverPath,
        trackIds = gson.fromJson(trackIdsJson, object : TypeToken<List<Int>>(){}.type) ?: emptyList()
    )
fun List<PlaylistEntity>.toDomain(gson: Gson) = map { it.toDomain(gson) }

fun PlaylistTrackEntity.toDomain(): Track = Track(
    trackId = trackId,
    artworkUrl100 = artworkUrl100,
    trackName = trackName,
    artistName = artistName,
    collectionName = collectionName.orEmpty(),
    releaseDate = releaseDate,
    primaryGenreName = primaryGenreName,
    country = country,
    trackTimeMillis = trackTimeMillis,
    previewUrl = previewUrl
)