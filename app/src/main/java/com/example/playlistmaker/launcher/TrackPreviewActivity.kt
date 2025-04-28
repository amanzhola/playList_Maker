package com.example.playlistmaker.launcher

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.playlistmaker.R
import com.example.playlistmaker.extraOption.OnTrackAudioClickListener
import com.example.playlistmaker.extraOption.TrackAdapterAudio
import com.example.playlistmaker.search.Track
import com.google.gson.Gson

//🎶👉💿🎧📀🎵👇
class TrackPreviewActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: TrackAdapterAudio

    private val viewModel: TrackPreviewViewModel by viewModels()
    private lateinit var snapHelper: PagerSnapHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_track_preview)

        if (savedInstanceState == null) {
            val json = intent.getStringExtra("track_list_json") ?: return
            viewModel.setTrackList(json)
            viewModel.setCurrentTrackIndex(intent.getIntExtra("track_index", 0))

        }

        recyclerView = findViewById(R.id.track_detail_recycler)
        snapHelper = PagerSnapHelper()
        snapHelper.attachToRecyclerView(recyclerView)

        adapter = TrackAdapterAudio(viewModel.trackList.value ?: emptyList(), object :
            OnTrackAudioClickListener {
            override fun onTrackClicked(track: Track, position: Int) {
                viewModel.setCurrentTrackIndex(position)
                viewModel.toggleIsHorizontal()
                viewModel.setScrollPosition(position)
                recyclerView.scrollToPosition(position)
            }

            override fun onBackArrowClicked() {
                viewModel.stopAudioPlay()
                finish()
            }

            override fun onPlayButtonClicked(track: Track) {
                viewModel.audioPlay(track)
            }
        },layoutId = R.layout.track_item2) // 💥 + audio to user

        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        viewModel.isHorizontal.observe(this) { isHorizontal ->
            recyclerView.layoutManager = LinearLayoutManager(
                this,
                if (isHorizontal) LinearLayoutManager.HORIZONTAL else LinearLayoutManager.VERTICAL,
                false
            )
        }

        viewModel.trackList.observe(this) { trackList ->
            adapter.update(trackList)
            val currentIndex = viewModel.currentTrackIndex.value ?: 0 // 📝 📂
            recyclerView.scrollToPosition(currentIndex) // 🎯 🎵
        } // 🔥

        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            @SuppressLint("UseKtx")
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)

                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    val position = (recyclerView.layoutManager as LinearLayoutManager)
                        .findFirstVisibleItemPosition()
                    viewModel.setCurrentTrackIndex(position)

                    val currentTrack = viewModel.trackList.value?.getOrNull(position)
                    if (currentTrack != null) {
                        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                        val trackJson = Gson().toJson(currentTrack)
                        prefs.edit().putString(TRACK_KEY, trackJson).apply()
                    }
                }
            }
        })

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    override fun onPause() {
        super.onPause()
        val currentPosition = (recyclerView.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
        viewModel.setScrollPosition(currentPosition)
    }

    companion object {
        const val PREFS_NAME = "SelectedTrackPrefs"
        const val TRACK_KEY = "selectedTrack"
    }
}