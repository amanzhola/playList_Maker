package com.example.playlistmaker.ui.audioPosters

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import android.view.View.VISIBLE
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.playlistmaker.BaseActivity
import com.example.playlistmaker.R
import com.example.playlistmaker.creator.Creator
import com.example.playlistmaker.domain.api.TrackStorageHelper
import com.example.playlistmaker.domain.models.Track
import com.example.playlistmaker.domain.repository.AudioSingleTrackShare
import com.example.playlistmaker.presentation.audioPostersViewModels.ExtraOptionViewModel
import com.example.playlistmaker.presentation.utils.ToolbarConfig

class ExtraOption : BaseActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: TrackAdapterAudio
    private lateinit var titleTextView: TextView
    private lateinit var toolbar: Toolbar
    private lateinit var viewModel: ExtraOptionViewModel
    private lateinit var snapHelper: PagerSnapHelper

    private var isBottomNavVisible: Boolean = true
    private lateinit var shareHelper: AudioSingleTrackShare
    private lateinit var trackStorageHelper: TrackStorageHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val factory = Creator.provideExtraOptionViewModelFactory()
        viewModel = ViewModelProvider(this, factory)[ExtraOptionViewModel::class.java]

        recyclerView = findViewById(R.id.tracks_recycler_view)

        snapHelper = PagerSnapHelper()
        snapHelper.attachToRecyclerView(recyclerView) // 1️⃣

        adapter = TrackAdapterAudio(viewModel.trackList.value ?: emptyList(), object :
            OnTrackAudioClickListener {
            override fun onTrackClicked(track: Track, position: Int) {
                viewModel.setCurrentTrackIndex(position)
                viewModel.toggleIsHorizontal()
                viewModel.setScrollPosition(position)
                recyclerView.scrollToPosition(position)
            }

            override fun onBackArrowClicked() {} //  👇

            override fun onPlayButtonClicked(track: Track) {
                viewModel.audioPlay(track) // ✨
            }
        })
        findViewById<TextView>(R.id.bottom6).isSelected = true
        shareHelper = Creator.provideShareHelper(this)
        trackStorageHelper = Creator.provideTrackStorageHelper(this)

        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this,
            LinearLayoutManager.HORIZONTAL, false)

        viewModel.isHorizontal.observe(this) { isHorizontal -> // 👀
            recyclerView.layoutManager = LinearLayoutManager(
                this, // 😎 👇
                if (isHorizontal) LinearLayoutManager.HORIZONTAL else LinearLayoutManager.VERTICAL,
                false
            )
            snapHelper.attachToRecyclerView(recyclerView)  // 1️⃣ 👉 🔄 2️⃣
        }

        viewModel.trackList.observe(this) { trackList -> // 👉 📊 📋 🎵 🎵 🎵
            adapter.update(trackList) // 👉 📊 ➡️ 👉 🔄

            val currentIndex = viewModel.currentTrackIndex.value ?: 0 // 📝 📂
            recyclerView.scrollToPosition(currentIndex) // 🎯 🎵
        }

        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() { // 📈
            @SuppressLint("UseKtx")
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)

                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    val position = (recyclerView.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
                    viewModel.setCurrentTrackIndex(position) // 🎵 👉 📦 💾
                    viewModel.setScrollPosition(position) // 💾 📥
                }
            }
        })

        viewModel.isBottomNavVisible.observe(this) { isVisible -> // 📝 📂
            recyclerView.visibility = if (isVisible) View.GONE else VISIBLE // 😕 🚗
        }

        if (savedInstanceState == null) { // 🎵 👉 📦 💾
            val inputData = Creator.provideTrackListIntentParser().parse(intent)
            inputData?.let {
                viewModel.initializeWith(it) // 📝 📂 + 📜 🎵
            }
        }
        titleAndHeight() // 🏆
    }

    override fun onPause() {
        super.onPause()
        if (::recyclerView.isInitialized) { // ✅
            val currentPosition = (recyclerView.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
            viewModel.setScrollPosition(currentPosition)  // 📝 📂 👉 📦 💾
        }
    }

    private fun titleAndHeight() {
        titleTextView = findViewById(R.id.title)
        toolbar = findViewById(R.id.toolbar)
        if (viewModel.isBottomNavVisible.value == false) titleTextView.visibility = View.INVISIBLE // 🚗
        val fixedHeightInDp = 45 // ❓
        val fixedHeightInPx = fixedHeightInDp.convertDpToPx(this)

        val layoutParams = toolbar.layoutParams
        layoutParams.height = fixedHeightInPx
        toolbar.layoutParams = layoutParams
    }

    private fun Int.convertDpToPx(context: Context): Int {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, toFloat(), context.resources.displayMetrics).toInt()
    }

    override fun onSegment4Clicked() {
        if (isBottomNavVisible) hideBottomNavigation()
        else showBottomNavigation()
        isBottomNavVisible = !isBottomNavVisible
    }

    override fun getToolbarConfig(): ToolbarConfig = ToolbarConfig(VISIBLE, R.string.option) {
        if (viewModel.isBottomNavVisible.value == true) navigateToMainActivity() else { // 💎
            viewModel.stopAudioPlay()
            finish()
        }
    }

    override fun shouldEnableEdgeToEdge(): Boolean = false // 💥
    override fun getLayoutId(): Int = R.layout.activity_extra_option
    override fun getMainLayoutId(): Int = R.id.main // 😎

    fun shareSingleTrack() { // 🎵
        val currentTrack = viewModel.getCurrentTrack()
        shareHelper.shareTrackOrNotify(currentTrack)
    }
}
