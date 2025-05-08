package com.example.playlistmaker.ui.launcherPosters

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.playlistmaker.R
import com.example.playlistmaker.ui.audio.OnTrackClickListener
import com.example.playlistmaker.domain.models.Track
import com.google.gson.Gson


class TrackDetailActivity : AppCompatActivity(), OnTrackClickListener {

    private lateinit var adapter: TrackAdapterTD
    private val tracks: MutableList<Track> = mutableListOf()
    private var trackIndex: Int = 0

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

        if (savedInstanceState != null) {
            val json = savedInstanceState.getString("track_list_json")
            trackIndex = savedInstanceState.getInt("track_index", 0)
            json?.let {
                tracks.addAll(Gson().fromJson(it, Array<Track>::class.java).toList())
            }
        } else {
            trackIndex = intent.getIntExtra("track_index", 0)
            val trackListJson = intent.getStringExtra("track_list_json")
            trackListJson?.let {
                tracks.addAll(Gson().fromJson(it, Array<Track>::class.java).toList())
            }
        }

        adapter = TrackAdapterTD(tracks, this)
        recyclerView.adapter = adapter
        recyclerView.scrollToPosition(trackIndex)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("track_list_json", Gson().toJson(tracks))
        outState.putInt("track_index", trackIndex)
    }

    override fun onTrackClicked(track: Track) {
        // TODO: Реализация клика по треку
    }

    override fun onArrowClicked(track: Track) {
        tracks.remove(track)
        adapter.updateTracks(tracks)
    }
}