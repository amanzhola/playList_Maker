package com.example.playlistmaker.ui.audioPosters

import android.content.Context
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.playlistmaker.BaseActivity
import com.example.playlistmaker.R
import com.example.playlistmaker.databinding.ActivityExtraOptionBinding
import com.example.playlistmaker.domain.models.search.Track
import com.example.playlistmaker.domain.repository.base.AudioSingleTrackShare
import com.example.playlistmaker.domain.repository.base.TrackListIntentParser
import com.example.playlistmaker.presentation.searchPostersViewModels.ExtraOptionViewModel
import com.example.playlistmaker.presentation.utils.ToolbarConfig
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class ExtraOption : BaseActivity() {

    private lateinit var binding: ActivityExtraOptionBinding
    private lateinit var adapter: TrackAdapterAudio
    private val viewModel: ExtraOptionViewModel by viewModel()
    private lateinit var snapHelper: PagerSnapHelper
    private val shareHelper: AudioSingleTrackShare by inject { parametersOf(this) } // 👨‍💻
    private val trackListIntentParser: TrackListIntentParser by inject() // 👉 📦 🔄

    private var currentLayoutOrientation: Int = LinearLayoutManager.HORIZONTAL

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityExtraOptionBinding.bind(findViewById(getMainLayoutId()))

        adapter = TrackAdapterAudio(emptyList(), object : OnTrackAudioClickListener {
            override fun onTrackClicked(track: Track, position: Int) {
                viewModel.setCurrentTrackIndex(position)
                viewModel.toggleIsHorizontal()
                viewModel.setScrollPosition(position)
            }

            override fun onPlayButtonClicked(track: Track) {
                viewModel.audioPlay(track)
            }

            override fun onBackArrowClicked() {}
        })

        binding.tracksRecyclerView.adapter = adapter
        snapHelper = PagerSnapHelper().also { it.attachToRecyclerView(binding.tracksRecyclerView) }
        setLayoutManager(currentLayoutOrientation)

        viewModel.state.observe(this) { state ->

            // Обновляем список треков, если изменился
            if (adapter.getItems() != state.trackList) {
                adapter.update(state.trackList.map { it.copy() })
                binding.tracksRecyclerView.scrollToPosition(state.currentTrackIndex)
            }

            // Обновляем ориентацию layoutManager'а при необходимости
            val desiredOrientation = if (state.isHorizontal) LinearLayoutManager.HORIZONTAL else LinearLayoutManager.VERTICAL
            if (desiredOrientation != currentLayoutOrientation) {
                currentLayoutOrientation = desiredOrientation
                setLayoutManager(desiredOrientation)
                binding.tracksRecyclerView.scrollToPosition(state.currentTrackIndex)
            }

            // Показываем/скрываем элементы навигации
            binding.tracksRecyclerView.visibility = if (state.isBottomNavVisible) View.GONE else View.VISIBLE
            findViewById<TextView>(R.id.title).visibility = if (state.isBottomNavVisible) View.VISIBLE else View.INVISIBLE

            findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)?.apply {
                val fixedHeightInPx = 45.convertDpToPx(this@ExtraOption)
                layoutParams.height = fixedHeightInPx
                requestLayout()
            }

        }

        // Сохраняем позицию скролла при остановке скроллинга
        binding.tracksRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    val position = (recyclerView.layoutManager as? LinearLayoutManager)?.findFirstVisibleItemPosition() ?: 0
                    viewModel.setCurrentTrackIndex(position)
                    viewModel.setScrollPosition(position)
                }
            }
        })

        // Обработка первого запуска
        if (savedInstanceState == null) {
            trackListIntentParser.parse(intent)?.let { viewModel.initializeWith(it) }
        }

        // Выделение кнопки в нижней навигации
        binding.root.findViewById<View>(R.id.bottom6).isSelected = true
    }

    override fun onPause() {
        super.onPause()
        val pos = (binding.tracksRecyclerView.layoutManager as? LinearLayoutManager)?.findFirstVisibleItemPosition() ?: 0
        viewModel.setScrollPosition(pos)
    }

    private fun setLayoutManager(orientation: Int) {
        binding.tracksRecyclerView.layoutManager = LinearLayoutManager(this, orientation, false)
        snapHelper.attachToRecyclerView(binding.tracksRecyclerView)
    }

    override fun getToolbarConfig(): ToolbarConfig = ToolbarConfig(View.VISIBLE, R.string.option) {
        if (viewModel.state.value?.isBottomNavVisible == true) {
            navigateToMainActivity()
        } else {
            viewModel.stopAudioPlay()
            finish()
        }
    }

    override fun shouldEnableEdgeToEdge(): Boolean = false
    override fun getLayoutId(): Int = R.layout.activity_extra_option
    override fun getMainLayoutId(): Int = R.id.main

    override fun onSegment4Clicked() {
        val visible = viewModel.state.value?.isBottomNavVisible == true
        viewModel.updateState { it.copy(isBottomNavVisible = !visible) }
    }

    fun shareSingleTrack() {
        viewModel.getCurrentTrack()?.let { shareHelper.shareTrackOrNotify(it) }
    }

    private fun Int.convertDpToPx(context: Context): Int =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, toFloat(), context.resources.displayMetrics).toInt()
}