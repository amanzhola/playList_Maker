package com.example.playlistmaker.data.repository

import android.content.SharedPreferences
import androidx.core.content.edit
import com.example.playlistmaker.domain.models.Track
import com.example.playlistmaker.domain.repository.SearchHistoryRepository
import com.google.gson.Gson

class SharedPreferencesSearchHistoryRepository(
    private val sharedPreferences: SharedPreferences
) : SearchHistoryRepository {

    private val gson = Gson()

    override fun getHistory(): List<Track> {
        val json = sharedPreferences.getString(TRACK_HISTORY_LIST_KEY, null)
        return if (!json.isNullOrEmpty()) {
            gson.fromJson(json, Array<Track>::class.java).toList()
        } else {
            emptyList()
        }
    }

    override fun addTrackToHistory(track: Track) {
        val current = getHistory().toMutableList()
        // Удаляем одинаковый трек, если есть
        current.removeAll { it.trackId == track.trackId }
        // Ограничение по длине
        if (current.size >= MAX_HISTORY_SIZE) {
            current.removeAt(current.lastIndex)
        }
        current.add(0, track)
        saveHistory(current)
    }

//    @SuppressLint("UseKtx")
//    override fun clearHistory() {
//        sharedPreferences.edit().remove(TRACK_HISTORY_LIST_KEY).apply()
//    }

    override fun clearHistory() {
        saveHistory(emptyList())
    }

//    @SuppressLint("UseKtx")
//    override fun saveHistory(list: List<Track>) {
//        val json = gson.toJson(list)
//        sharedPreferences.edit().putString(TRACK_HISTORY_LIST_KEY, json).apply()
//    }

    override fun saveHistory(tracks: List<Track>) {
        sharedPreferences.edit {
            putString(TRACK_HISTORY_LIST_KEY, gson.toJson(tracks))
        }
    }

    companion object {
        const val PREFS_NAME = "my_preferences" // 💥 with ExtraOption
        const val TRACK_HISTORY_LIST_KEY = "trackHistoryList"
        private const val MAX_HISTORY_SIZE = 10
    }
}