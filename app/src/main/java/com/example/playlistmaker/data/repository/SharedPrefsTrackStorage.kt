package com.example.playlistmaker.data.repository

import android.annotation.SuppressLint
import android.content.SharedPreferences
import com.example.playlistmaker.domain.api.TrackSerializer
import com.example.playlistmaker.domain.api.TrackStorageHelper
import com.example.playlistmaker.domain.models.Track

class SharedPrefsTrackStorage(
    private val sharedPreferences: SharedPreferences,
    private val serializer: TrackSerializer
) : TrackStorageHelper {

    companion object {
        private const val KEY_TRACK = "selectedTrack"
        private const val KEY_TRACK_LIST = "trackList"
        private const val KEY_TRACK_INDEX = "trackIndex"
    }

    @SuppressLint("UseKtx")
    override fun saveTrack(track: Track) {
        val json = serializer.serialize(track)
        sharedPreferences.edit().putString(KEY_TRACK, json).apply()
    }

    override fun getTrack(): Track? {
        val json = sharedPreferences.getString(KEY_TRACK, null)
        return json?.let { serializer.deserialize(it) }
    }

    @SuppressLint("UseKtx")
    override fun saveTrackList(tracks: List<Track>) {
        val json = serializer.serializeList(tracks)
        sharedPreferences.edit().putString(KEY_TRACK_LIST, json).apply()
    }

    override fun getTrackList(): List<Track> {
        val json = sharedPreferences.getString(KEY_TRACK_LIST, null)
        return json?.let { serializer.deserializeList(it) } ?: emptyList()
    }

    @SuppressLint("UseKtx")
    override fun setCurrentIndex(index: Int) {
        sharedPreferences.edit().putInt(KEY_TRACK_INDEX, index).apply()
    }

    override fun getCurrentIndex(): Int {
        return sharedPreferences.getInt(KEY_TRACK_INDEX, 0)
    }

    override fun parseTracksFromJson(json: String): List<Track>? {
        return serializer.deserializeList(json)
    }
}
