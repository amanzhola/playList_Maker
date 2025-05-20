package com.example.playlistmaker.data.repository

import com.example.playlistmaker.domain.api.TrackSerializer
import com.example.playlistmaker.domain.models.Track
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class GsonTrackSerializer(private val gson: Gson) : TrackSerializer {
    override fun serialize(track: Track): String = gson.toJson(track)

    override fun deserialize(json: String): Track = gson.fromJson(json, Track::class.java)

    override fun serializeList(tracks: List<Track>): String = gson.toJson(tracks)

    override fun deserializeList(json: String): List<Track> {
        val type = object : TypeToken<List<Track>>() {}.type
        return gson.fromJson(json, type)
    }
}
