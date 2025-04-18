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
import com.example.playlistmaker.extraOption.AudioPlayer
import com.example.playlistmaker.extraOption.OnTrackAudioClickListener
import com.example.playlistmaker.extraOption.TrackAdapterAudio
import com.example.playlistmaker.search.Track
import com.google.gson.Gson
//🎶👉💿🎧📀🎵👇
class TrackPreviewActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: TrackAdapterAudio

    private val viewModel: TrackPreviewViewModel by viewModels()
    private var isHorizontal = true
    private val audioPlayer = AudioPlayer()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_track_preview)

        val json = intent.getStringExtra("track_list_json") ?: return
        viewModel.setTrackList(json)
        viewModel.setCurrentTrackIndex(intent.getIntExtra("track_index", 0))

        isHorizontal = viewModel.isHorizontal
        val savedScrollPosition = viewModel.scrollPosition

        recyclerView = findViewById(R.id.track_detail_recycler)
        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        val snapHelper = PagerSnapHelper()
        snapHelper.attachToRecyclerView(recyclerView)

        viewModel.trackList.observe(this) { trackList ->
            adapter = TrackAdapterAudio(trackList, object : OnTrackAudioClickListener {
                override fun onTrackClicked(track: Track, position: Int) {
                    viewModel.setCurrentTrackIndex(position)

                    isHorizontal = !isHorizontal
                    viewModel.isHorizontal = isHorizontal

                    recyclerView.layoutManager = LinearLayoutManager(
                        this@TrackPreviewActivity,
                        if (isHorizontal) LinearLayoutManager.HORIZONTAL else LinearLayoutManager.VERTICAL,
                        false
                    )

                    viewModel.scrollPosition = position

                    snapHelper.attachToRecyclerView(null)
                    snapHelper.attachToRecyclerView(recyclerView)

                    recyclerView.scrollToPosition(position)
                }

                override fun onBackArrowClicked() {
                    finish()
                }
            }, audioPlayer, layoutId = R.layout.track_item2) // 💥 + audio to user

            recyclerView.adapter = adapter

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

            val scrollTo = if (savedScrollPosition != -1) savedScrollPosition
            else viewModel.currentTrackIndex.value ?: 0

            (recyclerView.layoutManager as LinearLayoutManager)
                .scrollToPositionWithOffset(scrollTo, 0)
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    override fun onPause() {
        super.onPause()
        val currentPosition = (recyclerView.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
        viewModel.scrollPosition = currentPosition
    }

    companion object {
        const val PREFS_NAME = "SelectedTrackPrefs"
        const val TRACK_KEY = "selectedTrack"
    }
}