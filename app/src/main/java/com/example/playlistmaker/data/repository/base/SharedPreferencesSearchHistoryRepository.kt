package com.example.playlistmaker.data.repository.base

import android.content.SharedPreferences
import androidx.core.content.edit
import com.example.playlistmaker.domain.models.search.Track
import com.example.playlistmaker.domain.repository.base.SearchHistoryRepository
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
        current.removeAll { it.trackId == track.trackId }
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

    override fun subscribeToHistoryChanges(callback: (List<Track>) -> Unit) {
        this.historyChangeCallback = callback
        sharedPreferences.registerOnSharedPreferenceChangeListener(prefsListener)
    }

    override fun unsubscribeFromHistoryChanges() {
        historyChangeCallback = null
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(prefsListener)
    }

    private var historyChangeCallback: ((List<Track>) -> Unit)? = null

    private val prefsListener =
        SharedPreferences.OnSharedPreferenceChangeListener { prefs, key ->
            if (key == TRACK_HISTORY_LIST_KEY) {
                val json = prefs.getString(TRACK_HISTORY_LIST_KEY, null)
                val history = if (!json.isNullOrEmpty()) {
                    gson.fromJson(json, Array<Track>::class.java).toList()
                } else {
                    emptyList()
                }
                historyChangeCallback?.invoke(history)
            }
        }
}