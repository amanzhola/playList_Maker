package com.example.playlistmaker.data.repository

import android.content.Intent
import com.example.playlistmaker.domain.models.Track
import com.example.playlistmaker.domain.models.TrackListInputData
import com.example.playlistmaker.domain.repository.TrackListIntentParser
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class TrackListIntentParserImpl : TrackListIntentParser {
    override fun parse(intent: Intent): TrackListInputData? {
        val json = intent.getStringExtra("TRACK_LIST_JSON") ?: return null
        val index = intent.getIntExtra("TRACK_INDEX", 0)
        val tracks = Gson().fromJson<List<Track>>(json, object : TypeToken<List<Track>>() {}.type)
        return TrackListInputData(tracks, index)
    }
}