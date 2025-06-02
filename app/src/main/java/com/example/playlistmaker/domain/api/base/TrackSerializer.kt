package com.example.playlistmaker.domain.api.base

import com.example.playlistmaker.domain.models.search.Track

interface TrackSerializer {
    fun serialize(track: Track): String
    fun deserialize(json: String): Track

    fun serializeList(tracks: List<Track>): String
    fun deserializeList(json: String): List<Track>?

}