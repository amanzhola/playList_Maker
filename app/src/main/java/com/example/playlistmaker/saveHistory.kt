package com.example.playlistmaker

import android.content.SharedPreferences
import androidx.core.content.edit
import com.google.gson.Gson

class SearchHistory(private val sharedPreferences: SharedPreferences) {

    companion object {
        private const val TRACK_HISTORY_LIST_KEY = "trackHistoryList"
    }

    var trackHistoryList: MutableList<Track> = mutableListOf()

    fun loadHistory() {
        val jsonTrackHistoryList = sharedPreferences.getString(TRACK_HISTORY_LIST_KEY, "")
        if (!jsonTrackHistoryList.isNullOrEmpty()) {
            trackHistoryList = Gson().fromJson(jsonTrackHistoryList, Array<Track>::class.java).toMutableList()
        }
    }

    fun saveHistory() {
        sharedPreferences.edit {
            putString(TRACK_HISTORY_LIST_KEY, Gson().toJson(trackHistoryList))
        }
    }

    fun addTrackToHistory(newTrack: Track) {
        if (trackHistoryList.size >= 10) {
            trackHistoryList.removeAt(trackHistoryList.lastIndex)
        }
        trackHistoryList = trackHistoryList.filter { it.trackId != newTrack.trackId }.toMutableList()
        trackHistoryList.add(0, newTrack)
    }

    fun clearHistory() {
        trackHistoryList.clear()
        saveHistory()
    }
}