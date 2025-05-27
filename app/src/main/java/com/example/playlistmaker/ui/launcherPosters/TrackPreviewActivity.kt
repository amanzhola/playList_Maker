package com.example.playlistmaker.ui.launcherPosters

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.playlistmaker.R
import com.example.playlistmaker.creator.Creator
import com.example.playlistmaker.domain.api.base.TrackStorageHelper
import com.example.playlistmaker.domain.models.search.Track
import com.example.playlistmaker.presentation.launcherViewModels.TrackPreviewViewModel
import com.example.playlistmaker.ui.audioPosters.OnTrackAudioClickListener
import com.example.playlistmaker.ui.audioPosters.TrackAdapterAudio

//🎶👉💿🎧📀🎵👇
class TrackPreviewActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: TrackAdapterAudio

    private lateinit var viewModel: TrackPreviewViewModel
    private lateinit var snapHelper: PagerSnapHelper
    private lateinit var trackStorageHelper: TrackStorageHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_track_preview)

        val factory = Creator.provideTrackPreviewViewModelFactory()
        trackStorageHelper = Creator.provideTrackStorageHelper(this)

        viewModel = ViewModelProvider(this, factory)[TrackPreviewViewModel::class.java]

        if (savedInstanceState == null) { // 🎵 👉 📦 💾
            val tracks = trackStorageHelper.getTrackList() // 📝 📂
            val index = trackStorageHelper.getCurrentIndex() // 📜 🎵
            viewModel.setTrackList(tracks)
            viewModel.setCurrentTrackIndex(index)
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
}
