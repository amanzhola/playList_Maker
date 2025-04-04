package com.example.playlistmaker

import android.os.Bundle
import android.view.View.VISIBLE
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.playlistmaker.extraOption.OnTrackAudioClickListener
import com.example.playlistmaker.extraOption.TrackAdapterAudio
import com.example.playlistmaker.search.Track
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class ExtraOption : BaseActivity() {

    private var isBottomNavVisible: Boolean = true
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: TrackAdapterAudio
    private lateinit var trackList: List<Track>
    private var currentTrackIndex: Int = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val json = intent.getStringExtra("TRACK_LIST_JSON")

        val type = object : TypeToken<List<Track>>() {}.type
        trackList = Gson().fromJson(json, type) ?: emptyList()
        currentTrackIndex = intent.getIntExtra("TRACK_INDEX", 0)

        recyclerView = findViewById(R.id.tracks_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        adapter = TrackAdapterAudio(trackList, object : OnTrackAudioClickListener {
            override fun onTrackClicked(track: Track) {
                // for future
            }
        })

        recyclerView.adapter = adapter

        if (currentTrackIndex in trackList.indices) {
            recyclerView.scrollToPosition(currentTrackIndex)
        }

        val isFromSearch = intent.getBooleanExtra("IS_FROM_SEARCH", false)
        isBottomNavVisible = !isFromSearch

        findViewById<TextView>(R.id.bottom6).isSelected = true
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("TRACK_LIST_JSON", Gson().toJson(trackList))
        outState.putBoolean("IS_BOTTOM_NAV_VISIBLE", isBottomNavVisible)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        val json = savedInstanceState.getString("TRACK_LIST_JSON")
        val type = object : TypeToken<List<Track>>() {}.type
        trackList = Gson().fromJson(json, type) ?: emptyList()
        isBottomNavVisible = savedInstanceState.getBoolean("IS_BOTTOM_NAV_VISIBLE", false)
    }

    override fun onSegment4Clicked() {
        if (isBottomNavVisible) {
            hideBottomNavigation()
        } else {
            showBottomNavigation()
        }
        isBottomNavVisible = !isBottomNavVisible
    }

    override fun getToolbarConfig(): ToolbarConfig {
        return ToolbarConfig(VISIBLE, R.string.Optoin2) { if (isBottomNavVisible) navigateToMainActivity() else finish() }
//        ToolbarConfig(VISIBLE, R.string.option) { navigateToMainActivity() }
    }

    override fun shouldEnableEdgeToEdge(): Boolean {
        return false
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_extra_option
    }

    override fun getMainLayoutId(): Int {
        return R.id.main
    }

}