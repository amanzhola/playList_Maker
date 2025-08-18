package com.example.playlistmaker.data.mappers

import com.example.playlistmaker.data.createPlaylist.PlaylistEntity
import com.example.playlistmaker.domain.models.playlist.Playlist
import com.google.firebase.crashlytics.buildtools.reloc.com.google.common.reflect.TypeToken
import com.google.gson.Gson

fun PlaylistEntity.toDomain(gson: Gson): Playlist =
    Playlist(
        id = id,
        name = name,
        description = description,
        coverPath = coverPath,
        tracksCount = tracksCount,
        trackIds = gson.fromJson(trackIdsJson, object : TypeToken<List<Int>>(){}.type) ?: emptyList()
    )
fun List<PlaylistEntity>.toDomain(gson: Gson) = map { it.toDomain(gson) }
