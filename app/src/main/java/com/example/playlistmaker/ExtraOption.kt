package com.example.playlistmaker

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import android.view.View.VISIBLE
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.playlistmaker.extraOption.OnTrackAudioClickListener
import com.example.playlistmaker.extraOption.TrackAdapterAudio
import com.example.playlistmaker.search.Track
import com.google.gson.Gson

class ExtraOption : BaseActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: TrackAdapterAudio
    private lateinit var titleTextView: TextView
    private lateinit var toolbar: Toolbar

    private val viewModel: ExtraOptionViewModel by viewModels()
    private var isHorizontal = true
    private val audioPlayer get() = viewModel.audioPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val json = intent.getStringExtra("TRACK_LIST_JSON") ?: return
        viewModel.setTrackList(json)
        viewModel.setCurrentTrackIndex(intent.getIntExtra("TRACK_INDEX", 0))

        isHorizontal = viewModel.isHorizontal
        val savedScrollPosition = viewModel.scrollPosition

        recyclerView = findViewById(R.id.tracks_recycler_view)
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
                        this@ExtraOption,
                        if (isHorizontal) LinearLayoutManager.HORIZONTAL else LinearLayoutManager.VERTICAL,
                        false
                    )

                    viewModel.scrollPosition = position

                    snapHelper.attachToRecyclerView(null)
                    snapHelper.attachToRecyclerView(recyclerView)

                    recyclerView.scrollToPosition(position)
                }

                override fun onBackArrowClicked(){
                    // case for single page
                }
            },audioPlayer)

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
                        currentTrack?.let {
                            val prefs = getSharedPreferences(PREFS_NAME1, Context.MODE_PRIVATE)
                            val trackJson = Gson().toJson(it)
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

        val isFromSearch = intent.getBooleanExtra("IS_FROM_SEARCH", false)
        viewModel.isBottomNavVisible = !isFromSearch

        titleAndHeight()
        findViewById<TextView>(R.id.bottom6).isSelected = true
    }

    override fun onStop() {
        super.onStop()
        audioPlayer.stop()
    }

    override fun onPause() {
        super.onPause()
        val currentPosition = (recyclerView.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
        viewModel.scrollPosition = currentPosition
    }

    private fun titleAndHeight(){
        titleTextView = findViewById(R.id.title)
        toolbar = findViewById(R.id.toolbar)
        if (!viewModel.isBottomNavVisible) titleTextView.visibility = View.INVISIBLE
        val fixedHeightInDp = 45
        val fixedHeightInPx = fixedHeightInDp.convertDpToPx(this)

        val layoutParams = toolbar.layoutParams
        layoutParams.height = fixedHeightInPx
        toolbar.layoutParams = layoutParams
    }

    private fun Int.convertDpToPx(context: Context): Int {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, toFloat(), context.resources.displayMetrics).toInt()
    }

    override fun onSegment4Clicked() {
        viewModel.toggleBottomNavVisibility()
        if (viewModel.isBottomNavVisible) hideBottomNavigation() else showBottomNavigation()
    }

    override fun getToolbarConfig(): ToolbarConfig = ToolbarConfig(VISIBLE, R.string.option)
    { if (viewModel.isBottomNavVisible) navigateToMainActivity() else finish() }
    override fun shouldEnableEdgeToEdge(): Boolean = false
    override fun getLayoutId(): Int = R.layout.activity_extra_option
    override fun getMainLayoutId(): Int = R.id.main

    companion object { // 😎
        const val PREFS_NAME1 = "SelectedTrackPrefs" // PREFS_NAME 💥 with History
        const val TRACK_KEY = "selectedTrack"
    }
}