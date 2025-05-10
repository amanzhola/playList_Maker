package com.example.playlistmaker.ui.audioPosters

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import android.view.View.VISIBLE
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.playlistmaker.BaseActivity
import com.example.playlistmaker.R
import com.example.playlistmaker.ToolbarConfig
import com.example.playlistmaker.creator.Creator
import com.example.playlistmaker.domain.api.AudioPlayerInteraction
import com.example.playlistmaker.domain.impl.AudioPlayerInteractionImpl
import com.example.playlistmaker.domain.models.Track
import com.example.playlistmaker.presentation.audioPostersViewModels.ExtraOptionViewModel
import com.example.playlistmaker.presentation.audioPostersViewModels.ExtraOptionViewModelFactory
import com.google.gson.Gson

class ExtraOption : BaseActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: TrackAdapterAudio
    private lateinit var titleTextView: TextView
    private lateinit var toolbar: Toolbar
    private lateinit var viewModel: ExtraOptionViewModel
    private lateinit var snapHelper: PagerSnapHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val audioPlayer = Creator.provideAudioPlayer()
        val factory = ExtraOptionViewModelFactory(audioPlayer)

        viewModel = ViewModelProvider(this, factory)[ExtraOptionViewModel::class.java]

        if (savedInstanceState == null) {   // 🎵 👉 📦 💾
            val json = intent.getStringExtra("TRACK_LIST_JSON") ?: return
            viewModel.setTrackList(json) // 📜 🎵
            viewModel.setCurrentTrackIndex(intent.getIntExtra("TRACK_INDEX", 0))
        }

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
                    viewModel.setCurrentTrackIndex(position)
                    viewModel.setScrollPosition(position)

                    val currentTrack = viewModel.trackList.value?.getOrNull(position)
                    currentTrack?.let { // 🎵 👉 📦 💾
                        val prefs = getSharedPreferences(PREFS_NAME1, Context.MODE_PRIVATE)  // 💾 📥
                        val trackJson = Gson().toJson(it)
                        prefs.edit().putString(TRACK_KEY, trackJson).apply()
                    }
                }
            }
        })

        // 📝 📂
        val isFromSearch = intent.getBooleanExtra("IS_FROM_SEARCH", false)
        viewModel.isBottomNavVisible = !isFromSearch // 😕 🚗
        titleAndHeight() // 🏆
    }

    override fun onPause() {
        super.onPause()
        val currentPosition = (recyclerView.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
        viewModel.setScrollPosition(currentPosition)  // 📝 📂 👉 📦 💾
    }

    private fun titleAndHeight() {
        titleTextView = findViewById(R.id.title)
        toolbar = findViewById(R.id.toolbar)
        if (!viewModel.isBottomNavVisible) titleTextView.visibility = View.INVISIBLE // 🚗
        val fixedHeightInDp = 45 // ❓
        val fixedHeightInPx = fixedHeightInDp.convertDpToPx(this)

        val layoutParams = toolbar.layoutParams
        layoutParams.height = fixedHeightInPx
        toolbar.layoutParams = layoutParams
    }

    private fun Int.convertDpToPx(context: Context): Int {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, toFloat(), context.resources.displayMetrics).toInt()
    }

    override fun getToolbarConfig(): ToolbarConfig = ToolbarConfig(VISIBLE, R.string.option) {
        if (viewModel.isBottomNavVisible) navigateToMainActivity() else { // 💎
            viewModel.stopAudioPlay()
            finish()

        }
    }

    override fun shouldEnableEdgeToEdge(): Boolean = false
    override fun getLayoutId(): Int = R.layout.activity_extra_option
    override fun getMainLayoutId(): Int = R.id.main

    companion object { // 😎
        const val PREFS_NAME1 = "SelectedTrackPrefs" // PREFS_NAME 💥 with History
        const val TRACK_KEY = "selectedTrack"
    }

    fun shareSingleTrack() { // 🎵
        val currentTrack = viewModel.getCurrentTrack()
        if (currentTrack == null) {
            // Обработка случая, когда трек не найден
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, getString(R.string.empty_track))
            }
            startActivity(Intent.createChooser(intent, null))
            return
        }

        // Создаём JSON файл для текущего трека
        val jsonFile = createJsonFile(listOf(currentTrack))
        val uri = FileProvider.getUriForFile(this, "${packageName}.fileprovider", jsonFile)

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/json"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_TEXT, getString(R.string.track_share))
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        startActivity(Intent.createChooser(shareIntent, null))
    }
}