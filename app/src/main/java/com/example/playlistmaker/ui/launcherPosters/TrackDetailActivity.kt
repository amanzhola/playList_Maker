package com.example.playlistmaker.ui.launcherPosters

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.playlistmaker.R
import com.example.playlistmaker.creator.Creator
import com.example.playlistmaker.domain.api.TrackStorageHelper
import com.example.playlistmaker.domain.models.Track
import com.example.playlistmaker.ui.audio.OnTrackClickListener


class TrackDetailActivity : AppCompatActivity(), OnTrackClickListener {

    private lateinit var adapter: TrackAdapterTD
    private val tracks: MutableList<Track> = mutableListOf()
    private var trackIndex: Int = 0
    private lateinit var trackStorageHelper: TrackStorageHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_track_detail)

        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.track_detail_title)

        toolbar.setNavigationOnClickListener { finish() }

        val recyclerView = findViewById<RecyclerView>(R.id.track_detail_recycler)
        recyclerView.layoutManager = LinearLayoutManager(this)
        trackStorageHelper = Creator.provideTrackStorageHelper(this)

        val savedTracks = trackStorageHelper.getTrackList()
        trackIndex = trackStorageHelper.getCurrentIndex()

        tracks.addAll(savedTracks)

        adapter = TrackAdapterTD(tracks, this)
        recyclerView.adapter = adapter
        recyclerView.scrollToPosition(trackIndex)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        adapter.cancelDebounce()
    }

    override fun onTrackClicked(track: Track) {
        // TODO: Реализация клика по треку
    }

    override fun onArrowClicked(track: Track) {
        tracks.remove(track)
        adapter.updateTracks(tracks)
    }
}