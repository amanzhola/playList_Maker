package com.example.playlistmaker.ui.launcherPosters

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.playlistmaker.R
import com.example.playlistmaker.domain.api.base.TrackStorageHelper
import com.example.playlistmaker.domain.models.search.Track
import com.example.playlistmaker.presentation.launcherViewModels.TrackPreviewViewModel
import com.example.playlistmaker.ui.audioPosters.OnTrackAudioClickListener
import com.example.playlistmaker.ui.audioPosters.TrackAdapterAudio
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class TrackPreviewActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: TrackAdapterAudio
    private val viewModel: TrackPreviewViewModel by viewModel()
    private lateinit var snapHelper: PagerSnapHelper
    private val trackStorageHelper: TrackStorageHelper by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_track_preview)

        recyclerView = findViewById(R.id.track_detail_recycler)
        snapHelper = PagerSnapHelper()
        adapter = TrackAdapterAudio(emptyList(), object : OnTrackAudioClickListener {
            override fun onTrackClicked(track: Track, position: Int) {
                viewModel.setCurrentTrackIndex(position)
                viewModel.toggleIsHorizontal()
                viewModel.setScrollPosition(position)
            }

            override fun onBackArrowClicked() {
                viewModel.stopAudioPlay()
                finish()
            }

            override fun onPlayButtonClicked(track: Track) {
                viewModel.audioPlay(track)
            }
        }, layoutId = R.layout.track_item2)

        recyclerView.adapter = adapter

        viewModel.state.observe(this) { state ->
            val oldList = adapter.getItems()
            val newList = state.trackList

            if (oldList != newList) {
                adapter.update(newList.map { it.copy() })

                recyclerView.post {
                    recyclerView.scrollToPosition(state.currentTrackIndex)
                }
            }

            val currentOrientation =
                (recyclerView.layoutManager as? LinearLayoutManager)?.orientation

            val newOrientation = if (state.isHorizontal)
                RecyclerView.HORIZONTAL
            else
                RecyclerView.VERTICAL

            if (currentOrientation != newOrientation) {
                val layoutManager = LinearLayoutManager(this, newOrientation, false)
                recyclerView.layoutManager = layoutManager

                snapHelper.attachToRecyclerView(null) // ⚠️ Сначала отцепляем
                snapHelper.attachToRecyclerView(recyclerView)

                // Скроллим к сохранённой позиции после смены ориентации
                if (state.scrollPosition >= 0) {
                    recyclerView.scrollToPosition(state.scrollPosition)
                }
            }

            findViewById<TextView>(R.id.title)?.visibility =
                if (state.trackList.isEmpty()) View.INVISIBLE else View.VISIBLE
        }

        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recycler: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recycler, newState)
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    val position =
                        (recycler.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
                    viewModel.setCurrentTrackIndex(position)
                    viewModel.setScrollPosition(position)
                }
            }
        })

        if (savedInstanceState == null) {
            val tracks = trackStorageHelper.getTrackList()
            val selectedIndex = intent.getIntExtra("track_index", 0)
            viewModel.initialize(tracks, selectedIndex)
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }
    }

    override fun onPause() {
        super.onPause()
        val currentPosition =
            (recyclerView.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
        viewModel.setScrollPosition(currentPosition)
    }
}
