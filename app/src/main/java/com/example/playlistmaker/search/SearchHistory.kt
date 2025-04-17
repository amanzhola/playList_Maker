package com.example.playlistmaker.search

import android.content.SharedPreferences
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.TextView
import androidx.core.content.edit
import com.google.android.material.button.MaterialButton
import com.google.gson.Gson

class SearchHistory(
    private val sharedPreferences: SharedPreferences,
    private val history: TextView? = null, // 💡 init
    private val update: MaterialButton? = null // 💡 init
) {
    private val gson = Gson()

    fun getHistory(): List<Track> {
        val json = sharedPreferences.getString(TRACK_HISTORY_LIST_KEY, "")
        return if (!json.isNullOrEmpty()) {
            gson.fromJson(json, Array<Track>::class.java).toList()
        } else {
            emptyList()
        }
    }

    fun saveHistory(tracks: List<Track>) {
        sharedPreferences.edit {
            putString(TRACK_HISTORY_LIST_KEY, gson.toJson(tracks))
        }
    }

    fun addTrackToHistory(newTrack: Track) {
        val currentHistory = getHistory().toMutableList()
        currentHistory.removeAll { it.trackId == newTrack.trackId }

        if (currentHistory.size >= 10) {
            currentHistory.removeAt(currentHistory.lastIndex)
        }

        currentHistory.add(0, newTrack)
        saveHistory(currentHistory)
    }

    private fun clearHistory() {
        saveHistory(emptyList())
    }

    fun showHistory() {
        history?.visibility = VISIBLE
        update?.visibility = VISIBLE

        update?.setOnClickListener {
            clearHistory()
            hideHistory()
        }
    }

    fun hideHistory() {
        history?.visibility = GONE
        update?.visibility = GONE
    }

    companion object {
        const val PREFS_NAME = "my_preferences" // 💥 with ExtraOption
        const val TRACK_HISTORY_LIST_KEY = "trackHistoryList"
    }
}