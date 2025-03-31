package com.example.playlistmaker.search

import android.content.SharedPreferences
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.TextView
import androidx.core.content.edit
import com.google.android.material.button.MaterialButton
import com.google.gson.Gson

class SearchHistory(private val sharedPreferences: SharedPreferences,
                    private val history: TextView,
                    private val update: MaterialButton){

    companion object {
        private const val TRACK_HISTORY_LIST_KEY = "trackHistoryList"
    }

    var trackHistoryList: MutableList<Track> = mutableListOf()

    fun loadHistory() {
        val jsonTrackHistoryList = sharedPreferences.getString(TRACK_HISTORY_LIST_KEY, "")
        if (!jsonTrackHistoryList.isNullOrEmpty()) {
            trackHistoryList =
                Gson().fromJson(jsonTrackHistoryList, Array<Track>::class.java).toMutableList()
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
        trackHistoryList =
            trackHistoryList.filter { it.trackId != newTrack.trackId }.toMutableList()
        trackHistoryList.add(0, newTrack)

        saveHistory()
    }

    private fun clearHistory() {
        trackHistoryList.clear()
        saveHistory()
    }

    fun showHistory(){
        history.visibility = VISIBLE
        update.visibility = VISIBLE

        update.setOnClickListener {
            clearHistory()
            hideHistory()
        }
    }

    fun hideHistory(){
        history.visibility = GONE
        update.visibility = GONE
    }

}


