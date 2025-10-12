package com.example.playlistmaker.ui.launcherPosters

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.playlistmaker.R
import com.example.playlistmaker.domain.api.base.TrackStorageHelper
import com.example.playlistmaker.domain.models.playlist.Playlist
import com.example.playlistmaker.domain.models.search.Track
import com.example.playlistmaker.presentation.ImageLoader
import com.example.playlistmaker.presentation.launcherViewModels.TrackPreviewViewModel
import com.example.playlistmaker.roots.main.MainActivity
import com.example.playlistmaker.ui.audioPosters.OnTrackAudioClickListener
import com.example.playlistmaker.ui.audioPosters.PlaylistBottomAdapter
import com.example.playlistmaker.ui.audioPosters.TrackAdapterAudio
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class TrackPreviewActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: TrackAdapterAudio
    private lateinit var snapHelper: PagerSnapHelper

    private val viewModel: TrackPreviewViewModel by viewModel()
    private val trackStorageHelper: TrackStorageHelper by inject()
    private val imageLoader: ImageLoader by inject()

    // ▼ Bottom sheet
    private lateinit var bottomBehavior: BottomSheetBehavior<LinearLayout>
    private lateinit var overlay: View
    private lateinit var bottomAdapter: PlaylistBottomAdapter

    // опционально: запомним трек, который хотим добавить
    private var pendingTrackForAdd: Track? = null

    // запуск CreatePlaylistFragment внутри MainActivity с результатом
    private val createLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { res ->
            if (res.resultCode == Activity.RESULT_OK && res.data != null) {
                val createdName = res.data!!.getStringExtra("playlist_created_name")
                if (!createdName.isNullOrEmpty()) {
                    showSnack(getString(R.string.playlist_created, createdName))
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_track_preview)

        viewModel.startFavoritesSyncIfNeeded()

        // ====== список превью треков ======
        recyclerView = findViewById(R.id.track_detail_recycler)
        snapHelper = PagerSnapHelper()

        adapter = TrackAdapterAudio(
            emptyList(),
            object : OnTrackAudioClickListener {

                override fun onSeekRequested(track: Track, positionMs: Long) {

                    // require update if needed for time bar on utube support audio old mob
//                    // Если сейчас привязано видео — игнорим (ползунок видео свой)
//                    if (videoBoundPosition != NO_VIDEO_POSITION) return
//                    viewModel.seekTo(positionMs) // need add seekTo to viewModel
                }

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

                override fun onFavoriteClicked(track: Track) { // ❤️
                    /* по желанию */
                    viewModel.onFavoriteClicked(track.trackId)
                }

                // 🎵➕ Add Track 👉💿 — показываем шторку
                override fun onAddTrackClicked(track: Track) {
                    pendingTrackForAdd = track
                    val sheet = findViewById<LinearLayout>(R.id.playlists_bottom_sheet)
                    sheet.post {
                        bottomBehavior.isFitToContents = false
                        bottomBehavior.halfExpandedRatio = 0.6f
                        bottomBehavior.skipCollapsed = false
                        bottomBehavior.state = BottomSheetBehavior.STATE_HALF_EXPANDED
                    }
                }
            },
            layoutId = R.layout.track_item2
        )
        recyclerView.adapter = adapter

        // ====== Bottom sheet ======
        val bottom = findViewById<LinearLayout>(R.id.playlists_bottom_sheet)
        overlay = findViewById(R.id.overlay)

        bottomAdapter = PlaylistBottomAdapter(imageLoader) { playlist: Playlist ->
            // Клик по плейлисту — попросим VM добавить текущий трек
            // если VM использует getCurrentTrack(), этого достаточно
            viewModel.onPlaylistClicked(playlist)
        }

        // для ⬇️ Автоскролл к началу при вставке нового плейлиста в позицию 0
        val rvBottom = findViewById<RecyclerView>(R.id.rvBottomPlaylists).apply {
            layoutManager = LinearLayoutManager(this@TrackPreviewActivity)
            adapter = bottomAdapter
        }

        // ⬇️ Автоскролл к началу при вставке нового плейлиста в позицию 0
        // наблюдатель адаптера:Автоскролл к началу, когда в список прилетел новый элемент сверху
        bottomAdapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                if (positionStart == 0) {
                    rvBottom.post { rvBottom.scrollToPosition(0) }
                }
            }
        })

        findViewById<RecyclerView>(R.id.rvBottomPlaylists).apply {
            layoutManager = LinearLayoutManager(this@TrackPreviewActivity)
            adapter = bottomAdapter
        }

        findViewById<View>(R.id.btnUpdate).setOnClickListener {
            // «Новый плейлист» → скрываем шторку и идём в Create (через MainActivity)
            bottomBehavior.state = BottomSheetBehavior.STATE_HIDDEN
            openCreatePlaylist()
        }

        bottomBehavior = BottomSheetBehavior.from(bottom).apply {
            state = BottomSheetBehavior.STATE_HIDDEN
        }

        bottomBehavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(sheet: View, newState: Int) {
                when (newState) {
                    BottomSheetBehavior.STATE_HIDDEN -> {
                        overlay.isGone = true
                        overlay.alpha = 0f
                    }
                    BottomSheetBehavior.STATE_COLLAPSED -> {
                        overlay.isVisible = false
                        overlay.alpha = 0f
                    }
                    BottomSheetBehavior.STATE_HALF_EXPANDED -> {
                        overlay.isVisible = true
                        overlay.alpha = 0.6f
                    }
                    BottomSheetBehavior.STATE_EXPANDED -> {
                        overlay.isVisible = true
                        overlay.alpha = 1f
                    }
                    BottomSheetBehavior.STATE_DRAGGING,
                    BottomSheetBehavior.STATE_SETTLING -> Unit
                }
            }

            override fun onSlide(sheet: View, slideOffset: Float) {
                val t = slideOffset.coerceIn(0f, 1f)
                overlay.alpha = t
                overlay.isVisible = t > 0f
            }
        })

        // тап по затемнению — закрыть шторку
        overlay.setOnClickListener { bottomBehavior.state = BottomSheetBehavior.STATE_HIDDEN }

        // ====== Insets ======
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        // ====== Подписки ======
        // a) состояние экрана плеера (твоя логика)
        viewModel.state.observe(this) { state ->
            val oldList = adapter.getItems()
            val newList = state.trackList
            if (oldList != newList) {
                adapter.update(newList.map { it.copy() })
                recyclerView.post { recyclerView.scrollToPosition(state.currentTrackIndex) }
            }

            val lm = recyclerView.layoutManager as? LinearLayoutManager
            val current = lm?.orientation
            val desired = if (state.isHorizontal) RecyclerView.HORIZONTAL else RecyclerView.VERTICAL
            if (current != desired) {
                recyclerView.layoutManager = LinearLayoutManager(this, desired, false)
                snapHelper.attachToRecyclerView(null) // ⚠️ Сначала отцепляем
                snapHelper.attachToRecyclerView(recyclerView)
                if (state.scrollPosition >= 0) {
                    recyclerView.scrollToPosition(state.scrollPosition)
                }
            }

            findViewById<TextView>(R.id.title)?.visibility =
                if (state.trackList.isEmpty()) View.INVISIBLE else View.VISIBLE
        }

        // b) плейлисты + события добавления (если VM уже это умеет — как во фрагменте)
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.playlists.collect { list ->
                        bottomAdapter.submitList(list)
                    }
                }
                launch {
                    viewModel.playlistEvents.collect { e ->
                        when (e) {
                            is TrackPreviewViewModel.PlaylistEvent.Added -> {
                                bottomBehavior.state = BottomSheetBehavior.STATE_HIDDEN
                                showSnack(getString(R.string.added_to_playlist, e.playlistName))
                            }
                            is TrackPreviewViewModel.PlaylistEvent.AlreadyExists -> {
                                showSnack(getString(R.string.track_already_in_playlist, e.playlistName))
                            }
                            is TrackPreviewViewModel.PlaylistEvent.Error -> {
                                showSnack(e.message)
                            }
                        }
                    }
                }
            }
        }

        // ====== Скролл слушатель ======
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

        // ====== Инициализация данных ======
        if (savedInstanceState == null) {
            val tracks = trackStorageHelper.getTrackList()
            val selectedIndex = intent.getIntExtra("track_index", 0)
            viewModel.initialize(tracks, selectedIndex)
        }
    }

    override fun onPause() {
        super.onPause()
        val currentPosition =
            (recyclerView.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
        viewModel.setScrollPosition(currentPosition)
    }

    // ───────────────────────── helpers ─────────────────────────

    private fun openCreatePlaylist() {
        val focusId = viewModel.getCurrentTrack()?.trackId // если реализовано в VM
        val intent = Intent(this, MainActivity::class.java).apply {
            putExtra("open_create_playlist", true)
            putExtra("return_result", true) // попросим вернуть результат сюда
            if (focusId != null) putExtra("preview_track_id", focusId)
        }
        createLauncher.launch(intent)
    }

    private fun showSnack(text: String, durationMs: Int = 4000) {
        val root = findViewById<View>(android.R.id.content)
        val sb = Snackbar.make(root, text, Snackbar.LENGTH_LONG)
        (sb.view.layoutParams as? ViewGroup.MarginLayoutParams)?.setMargins(0, 0, 0, 0)
        sb.duration = durationMs
        sb.show()
    }
}
