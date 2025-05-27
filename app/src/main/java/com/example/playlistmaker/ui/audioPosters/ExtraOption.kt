//package com.example.playlistmaker.ui.audioPosters
//
//import android.annotation.SuppressLint
//import android.content.Context
//import android.os.Bundle
//import android.util.TypedValue
//import android.view.View
//import android.widget.TextView
//import androidx.lifecycle.ViewModelProvider
//import androidx.recyclerview.widget.LinearLayoutManager
//import androidx.recyclerview.widget.PagerSnapHelper
//import androidx.recyclerview.widget.RecyclerView
//import com.example.playlistmaker.BaseActivity
//import com.example.playlistmaker.R
//import com.example.playlistmaker.creator.Creator
//import com.example.playlistmaker.databinding.ActivityExtraOptionBinding
//import com.example.playlistmaker.domain.api.base.TrackStorageHelper
//import com.example.playlistmaker.domain.models.search.Track
//import com.example.playlistmaker.domain.repository.base.AudioSingleTrackShare
//import com.example.playlistmaker.presentation.searchPostersViewModels.ExtraOptionViewModel
//import com.example.playlistmaker.presentation.utils.ToolbarConfig
//
//class ExtraOption : BaseActivity() {
//
//    private lateinit var binding: ActivityExtraOptionBinding
//    private lateinit var adapter: TrackAdapterAudio
//    private lateinit var viewModel: ExtraOptionViewModel
//    private lateinit var snapHelper: PagerSnapHelper
//
//    private var isBottomNavVisible: Boolean = true
//    private lateinit var shareHelper: AudioSingleTrackShare
//    private lateinit var trackStorageHelper: TrackStorageHelper
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        binding = ActivityExtraOptionBinding.bind(findViewById(getMainLayoutId()))
//
//        val factory = Creator.provideExtraOptionViewModelFactory()
//        viewModel = ViewModelProvider(this, factory)[ExtraOptionViewModel::class.java]
//
//        snapHelper = PagerSnapHelper()
//        snapHelper.attachToRecyclerView(binding.tracksRecyclerView)
//
//        adapter = TrackAdapterAudio(emptyList(), object : OnTrackAudioClickListener {
//            override fun onTrackClicked(track: Track, position: Int) {
//                viewModel.setCurrentTrackIndex(position)
//                viewModel.toggleIsHorizontal()
//                viewModel.setScrollPosition(position)
//                binding.tracksRecyclerView.scrollToPosition(position)
//            }
//
//            override fun onBackArrowClicked() {}
//
//            override fun onPlayButtonClicked(track: Track) {
//                viewModel.audioPlay(track)
//            }
//        })
//
//        binding.root.findViewById<View>(R.id.bottom6).isSelected = true
//        shareHelper = Creator.provideShareHelper(this)
//        trackStorageHelper = Creator.provideTrackStorageHelper(this)
//
//        binding.tracksRecyclerView.adapter = adapter
//        binding.tracksRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
//
//        viewModel.screenState.observe(this) { state ->
//            adapter.update(state.trackList)
//
//            binding.tracksRecyclerView.layoutManager = LinearLayoutManager(
//                this,
//                if (state.isHorizontal) LinearLayoutManager.HORIZONTAL else LinearLayoutManager.VERTICAL,
//                false
//            )
//            snapHelper.attachToRecyclerView(binding.tracksRecyclerView)
//
//            binding.tracksRecyclerView.scrollToPosition(state.currentTrackIndex)
//
//            binding.tracksRecyclerView.visibility = if (state.isBottomNavVisible) View.GONE else View.VISIBLE
//
//            val titleTextView = findViewById<TextView>(R.id.title)
//            titleTextView.visibility = if (!state.isBottomNavVisible) View.INVISIBLE else View.VISIBLE
//        }
//
//        binding.tracksRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
//            @SuppressLint("UseKtx")
//            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
//                super.onScrollStateChanged(recyclerView, newState)
//                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
//                    val position = (recyclerView.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
//                    viewModel.setCurrentTrackIndex(position)
//                    viewModel.setScrollPosition(position)
//                }
//            }
//        })
//
//        if (savedInstanceState == null) {
//            val inputData = Creator.provideTrackListIntentParser().parse(intent)
//            inputData?.let {
//                viewModel.initializeWith(it)
//            }
//        }
//
//        titleAndHeight()
//    }
//
//    override fun onPause() {
//        super.onPause()
//        val currentPosition = (binding.tracksRecyclerView.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
//        viewModel.setScrollPosition(currentPosition)
//    }
//
//    private fun titleAndHeight() {
//        val titleTextView = findViewById<TextView>(R.id.title)
//        viewModel.screenState.value?.let { state ->
//            if (!state.isBottomNavVisible) {
//                titleTextView.visibility = View.INVISIBLE
//            }
//        }
//
//        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
//        toolbar?.let {
//            val fixedHeightInPx = 45.convertDpToPx(this)
//            val layoutParams = it.layoutParams
//            layoutParams.height = fixedHeightInPx
//            it.layoutParams = layoutParams
//        }
//    }
//
//    private fun Int.convertDpToPx(context: Context): Int {
//        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, toFloat(), context.resources.displayMetrics).toInt()
//    }
//
//    override fun onSegment4Clicked() {
//        if (isBottomNavVisible) hideBottomNavigation()
//        else showBottomNavigation()
//        isBottomNavVisible = !isBottomNavVisible
//    }
//
//    override fun getToolbarConfig(): ToolbarConfig = ToolbarConfig(View.VISIBLE, R.string.option) {
//        val state = viewModel.screenState.value
//        if (state?.isBottomNavVisible == true) {
//            navigateToMainActivity()
//        } else {
//            viewModel.stopAudioPlay()
//            finish()
//        }
//    }
//
//    override fun shouldEnableEdgeToEdge(): Boolean = false
//    override fun getLayoutId(): Int = R.layout.activity_extra_option
//    override fun getMainLayoutId(): Int = R.id.main
//
//    fun shareSingleTrack() {
//        val currentTrack = viewModel.getCurrentTrack()
//        shareHelper.shareTrackOrNotify(currentTrack)
//    }
//}
//
//
package com.example.playlistmaker.ui.audioPosters

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.playlistmaker.BaseActivity
import com.example.playlistmaker.R
import com.example.playlistmaker.creator.Creator
import com.example.playlistmaker.databinding.ActivityExtraOptionBinding
import com.example.playlistmaker.domain.api.base.TrackStorageHelper
import com.example.playlistmaker.domain.models.search.Track
import com.example.playlistmaker.domain.repository.base.AudioSingleTrackShare
import com.example.playlistmaker.presentation.searchPostersViewModels.ExtraOptionViewModel
import com.example.playlistmaker.presentation.utils.ToolbarConfig

class ExtraOption : BaseActivity() {

    private lateinit var binding: ActivityExtraOptionBinding
    private lateinit var adapter: TrackAdapterAudio
    private lateinit var viewModel: ExtraOptionViewModel
    private lateinit var snapHelper: PagerSnapHelper

    private var isBottomNavVisible: Boolean = true
    private lateinit var shareHelper: AudioSingleTrackShare
    private lateinit var trackStorageHelper: TrackStorageHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityExtraOptionBinding.bind(findViewById(getMainLayoutId()))

        val factory = Creator.provideExtraOptionViewModelFactory()
        viewModel = ViewModelProvider(this, factory)[ExtraOptionViewModel::class.java]

        snapHelper = PagerSnapHelper()
        snapHelper.attachToRecyclerView(binding.tracksRecyclerView) // 1️⃣

        adapter = TrackAdapterAudio(viewModel.trackList.value ?: emptyList(), object : OnTrackAudioClickListener {
            override fun onTrackClicked(track: Track, position: Int) {
                viewModel.setCurrentTrackIndex(position)
                viewModel.toggleIsHorizontal()
                viewModel.setScrollPosition(position)
                binding.tracksRecyclerView.scrollToPosition(position)
            }

            override fun onBackArrowClicked() {} //  👇

            override fun onPlayButtonClicked(track: Track) {
                viewModel.audioPlay(track) // ✨
            }
        })

        binding.root.findViewById<View>(R.id.bottom6).isSelected = true
        shareHelper = Creator.provideShareHelper(this)
        trackStorageHelper = Creator.provideTrackStorageHelper(this)

        binding.tracksRecyclerView.adapter = adapter
        binding.tracksRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        viewModel.isHorizontal.observe(this) { isHorizontal -> // 👀
            binding.tracksRecyclerView.layoutManager = LinearLayoutManager(
                this, // 😎 👇
                if (isHorizontal) LinearLayoutManager.HORIZONTAL else LinearLayoutManager.VERTICAL,
                false
            )
            snapHelper.attachToRecyclerView(binding.tracksRecyclerView) // 1️⃣ 👉 🔄 2️⃣
        }

        viewModel.trackList.observe(this) { trackList -> // 👉 📊 📋 🎵 🎵 🎵
            adapter.update(trackList)  // 👉 📊 ➡️ 👉 🔄
            val currentIndex = viewModel.currentTrackIndex.value ?: 0 // 📝 📂
            binding.tracksRecyclerView.scrollToPosition(currentIndex) // 🎯 🎵
        }

        binding.tracksRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            @SuppressLint("UseKtx") // 📈
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
            binding.tracksRecyclerView.visibility = if (isVisible) View.GONE else View.VISIBLE // 😕 🚗
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
        super.onPause() // ✅
        val currentPosition = (binding.tracksRecyclerView.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
        viewModel.setScrollPosition(currentPosition)  // 📝 📂 👉 📦 💾
    }

    private fun titleAndHeight() { // ✨ 🔝 ✋
        val titleTextView = findViewById<TextView>(R.id.title)
        if (viewModel.isBottomNavVisible.value == false) {
            titleTextView.visibility = View.INVISIBLE
        }

        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        toolbar?.let {
            val fixedHeightInPx = 45.convertDpToPx(this) // ❓
            val layoutParams = it.layoutParams
            layoutParams.height = fixedHeightInPx
            it.layoutParams = layoutParams
        }
    }


    private fun Int.convertDpToPx(context: Context): Int {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, toFloat(), context.resources.displayMetrics).toInt()
    }

    override fun onSegment4Clicked() {
        if (isBottomNavVisible) hideBottomNavigation()
        else showBottomNavigation()
        isBottomNavVisible = !isBottomNavVisible
    }

    override fun getToolbarConfig(): ToolbarConfig = ToolbarConfig(View.VISIBLE, R.string.option) {
        if (viewModel.isBottomNavVisible.value == true) navigateToMainActivity() else {
            viewModel.stopAudioPlay()
            finish()
        } // 💎
    }

    override fun shouldEnableEdgeToEdge(): Boolean = false // 💥
    override fun getLayoutId(): Int = R.layout.activity_extra_option
    override fun getMainLayoutId(): Int = R.id.main // 😎

    fun shareSingleTrack() { // 🎵 💖
        val currentTrack = viewModel.getCurrentTrack()
        shareHelper.shareTrackOrNotify(currentTrack)
    }
}
