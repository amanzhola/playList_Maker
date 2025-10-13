package com.example.playlistmaker.ui.audioPosters

import android.Manifest
import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.playlistmaker.BaseActivity
import com.example.playlistmaker.BaseFragment
import com.example.playlistmaker.R
import com.example.playlistmaker.databinding.FragmentExtraOptionBinding
import com.example.playlistmaker.domain.api.base.NetworkStatusChecker
import com.example.playlistmaker.domain.models.search.Track
import com.example.playlistmaker.domain.repository.base.AudioSingleTrackShare
import com.example.playlistmaker.domain.repository.base.TrackListIntentParser
import com.example.playlistmaker.presentation.ImageLoader
import com.example.playlistmaker.presentation.searchPostersViewModels.ExtraOptionViewModel
import com.example.playlistmaker.presentation.utils.ToolbarConfig
import com.example.playlistmaker.roots.main.MainActivity
import com.example.playlistmaker.services.MusicService
import com.example.playlistmaker.ui.main.BottomNavConfig
import com.example.playlistmaker.utils.EXTRA_ARTIST
import com.example.playlistmaker.utils.EXTRA_ID
import com.example.playlistmaker.utils.EXTRA_TITLE
import com.example.playlistmaker.utils.EXTRA_URL
import com.example.playlistmaker.utils.NO_VIDEO_POSITION
import com.example.playlistmaker.utils.NavKeys
import com.example.playlistmaker.utils.showLongSnack
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class AudioPlayerFragment : BaseFragment(), BottomNavConfig {

    // Прокси на VM
    private val viewModel: ExtraOptionViewModel by viewModel()
    private val exo: ExoPlayer? get() = viewModel.exo
    private var videoBoundPosition: Int
        get() = viewModel.videoPos
        set(value) { viewModel.videoPos = value }

    private var videoTickerJob: Job? = null
    private var videoPlayerListener: Player.Listener? = null // for video

    private val networkChecker: NetworkStatusChecker by inject { parametersOf(requireContext()) }
    private var cm: ConnectivityManager? = null
    private var networkCallback: ConnectivityManager.NetworkCallback? = null
    private var lastConnected: Boolean? = null

    private lateinit var binding: FragmentExtraOptionBinding
    private lateinit var adapter: TrackAdapterAudio
    private lateinit var snapHelper: PagerSnapHelper

    private val shareHelper: AudioSingleTrackShare by inject { parametersOf(requireActivity()) }
    private val trackListIntentParser: TrackListIntentParser by inject()

    private var currentLayoutOrientation = LinearLayoutManager.HORIZONTAL
    private var isFromSearch: Boolean = false

    private lateinit var bottomBehavior: BottomSheetBehavior<LinearLayout>
    private lateinit var overlay: View
    private val imageLoader: ImageLoader by inject()
    private val bottomAdapter by lazy { PlaylistBottomAdapter(imageLoader) { viewModel.onPlaylistClicked(it) } }

    // MusicService binding
    private var musicService: MusicService? = null
    private var isBound: Boolean = false

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as? MusicService.MusicServiceBinder ?: return
            musicService = binder.getService()
            isBound = true
            viewModel.setAudioPlayerControl(musicService!!)
        }
        override fun onServiceDisconnected(name: ComponentName?) {
            isBound = false
            musicService = null
            viewModel.removeAudioPlayerControl()
        }
    }

    // уведомления (Android 13+)
    private val requestNotifPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) showSnack(getString(R.string.permission_notifications_granted))
            else showSnack(getString(R.string.permission_notifications_denied))
        }

    private fun buildBindIntentForCurrent(): Intent {
        val ctx = requireContext()
        val intent = Intent(ctx, MusicService::class.java)
        viewModel.getCurrentTrack()?.let { t ->
            intent.putExtra(EXTRA_URL, t.previewUrl)
            intent.putExtra(EXTRA_ARTIST, t.artistName)
            intent.putExtra(EXTRA_TITLE, t.trackName)
            intent.putExtra(EXTRA_ID, t.trackId)
        }
        return intent
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isFromSearch = arguments?.getBoolean("IS_FROM_SEARCH", false) ?: false
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentExtraOptionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val bottom = view.findViewById<LinearLayout>(R.id.playlists_bottom_sheet)
        overlay = view.findViewById(R.id.overlay)

        val rv = view.findViewById<RecyclerView>(R.id.rvBottomPlaylists)
        rv.layoutManager = LinearLayoutManager(requireContext())
        rv.adapter = bottomAdapter
        bottomAdapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                if (positionStart == 0) rv.post { rv.scrollToPosition(0) }
            }
        })

        view.findViewById<View>(R.id.btnUpdate).setOnClickListener {
            bottomBehavior.state = BottomSheetBehavior.STATE_HIDDEN
            findNavController().navigate(R.id.action_global_to_createPlaylistFragment)
        }

        bottomBehavior = BottomSheetBehavior.from(bottom).apply { state = BottomSheetBehavior.STATE_HIDDEN }
        bottomBehavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(sheet: View, newState: Int) {
                when (newState) {
                    BottomSheetBehavior.STATE_HIDDEN -> { overlay.isGone = true; overlay.alpha = 0f }
                    BottomSheetBehavior.STATE_COLLAPSED -> { overlay.isVisible = false; overlay.alpha = 0f }
                    BottomSheetBehavior.STATE_HALF_EXPANDED -> { overlay.isVisible = true; overlay.alpha = 0.6f }
                    BottomSheetBehavior.STATE_EXPANDED -> { overlay.isVisible = true; overlay.alpha = 1f }
                    BottomSheetBehavior.STATE_DRAGGING, BottomSheetBehavior.STATE_SETTLING -> Unit
                }
            }
            override fun onSlide(sheet: View, slideOffset: Float) {
                val t = slideOffset.coerceIn(0f, 1f)
                overlay.alpha = t.coerceAtMost(1f)
                overlay.isVisible = t > 0f
            }
        })

        // one-shot toast after playlist created
        val handle = findNavController().currentBackStackEntry?.savedStateHandle
        handle?.getLiveData<String>(NavKeys.PLAYLIST_CREATED_NAME)
            ?.observe(viewLifecycleOwner) { name ->
                if (::bottomBehavior.isInitialized) {
                    bottomBehavior.state = BottomSheetBehavior.STATE_HIDDEN
                    overlay.visibility = View.GONE
                    overlay.alpha = 0f
                }
                showSnack(getString(R.string.playlist_created, name), durationMs = 4000)
                handle.remove<String>(NavKeys.PLAYLIST_CREATED_NAME)
            }

        // Adapter
        adapter = TrackAdapterAudio(emptyList(), object : OnTrackAudioClickListener {

            override fun onSeekRequested(track: Track, positionMs: Long) {
                // Если играет видео — игнорим (ползунок скрыт)
                if (videoBoundPosition != NO_VIDEO_POSITION) return
                viewModel.seekTo(positionMs)
            }

            override fun onTrackClicked(track: Track, position: Int) {
                if (videoBoundPosition != NO_VIDEO_POSITION && videoBoundPosition != position) {
                    detachCurrentVideo()
                    stopAndReleaseVideo()
                }
                viewModel.setCurrentTrackIndex(position)
                viewModel.toggleIsHorizontal()
                viewModel.setScrollPosition(position)
            }

            override fun onPlayButtonClicked(track: Track) {
                // индекс карточки, по которой кликнули
                val newIndex = adapter.getItems().indexOfFirst { it.trackId == track.trackId }
                if (newIndex == RecyclerView.NO_POSITION) return

                val oldIndex = viewModel.state.value.currentTrackIndex

                // 1) если клик по самой видео-карточке — управляем видео и выходим
                if (videoBoundPosition == newIndex && exo != null) {
                    togglePlayPauseForCurrent()
                    return
                }

                // 2) если видео прикреплено к ДРУГОЙ карточке — сначала полностью выключаем ВИДЕО
                if (videoBoundPosition != NO_VIDEO_POSITION && videoBoundPosition != newIndex) {
                    val wasVideoPos = videoBoundPosition

                    // 🔸 оптимистично сбрасываем UI у бывшей видеокарточки
                    viewModel.updatePlayingUiForIndex(wasVideoPos, false)
                    adapter.updatePlayTimeTextAt(binding.tracksRecyclerView, wasVideoPos, "0:00")

                    // и уже потом реально отцепляем/останавливаем видео
                    detachCurrentVideo()
                    stopAndReleaseVideo()
                }

                // 3) если переходим на ДРУГОЙ аудио-трек — пересаживаем текущий индекс
                if (newIndex != oldIndex) {
                    viewModel.setCurrentTrackIndex(newIndex)
                    viewModel.setScrollPosition(newIndex)

                    // 🔸 оптимистичное переключение кнопок: старую гасим, новую зажигаем
                    viewModel.updatePlayingUiForIndex(oldIndex, false)
                    viewModel.updatePlayingUiForIndex(newIndex, true)

                    // стартуем аудио нового трека
                    viewModel.getCurrentTrack()?.let { cur ->
                        viewModel.audioPlay(cur)
                    }
                    return
                }

                // 4) клик по этой же карточке → обычный toggle
                togglePlayPauseForCurrent()
            }

            override fun onBackArrowClicked() {
                detachCurrentVideo()
                stopAndReleaseVideo()
                viewModel.stopAudioPlay()
                requireActivity().onBackPressedDispatcher.onBackPressed()
            }
            override fun onFavoriteClicked(track: Track) {
                viewModel.onFavoriteClicked(track.id)
            }
            override fun onAddTrackClicked(track: Track) {
                bottom.post {
                    bottomBehavior.isFitToContents = false
                    bottomBehavior.halfExpandedRatio = 0.6f
                    bottomBehavior.skipCollapsed = false
                    bottomBehavior.state = BottomSheetBehavior.STATE_HALF_EXPANDED
                }
            }
        })
        adapter.setTimebarVertical(currentLayoutOrientation == LinearLayoutManager.HORIZONTAL)

        // RecyclerView
        binding.tracksRecyclerView.adapter = adapter
        snapHelper = PagerSnapHelper().also { it.attachToRecyclerView(binding.tracksRecyclerView) }
        setLayoutManager(currentLayoutOrientation)

        // Переаттач видео после поворота
        adapter.setVideoPlayer(exo)
        if (videoBoundPosition != NO_VIDEO_POSITION && exo != null) {
            adapter.setVideoBoundPosition(videoBoundPosition)
            binding.tracksRecyclerView.post {
                adapter.attachVideoAt(binding.tracksRecyclerView, videoBoundPosition, exo!!)
                updateTimesForVisibleItems()
            }
            if (exo?.isPlaying == true) startVideoTicker()
        } else {
            updateTimesForVisibleItems()
        }

        // State subscriptions
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.state.collect { state ->

                        // --- ЗАМЕНА блока ---
                        val old = adapter.getItems()
                        val new = state.trackList

                        val oldIds = old.map { it.trackId }
                        val newIds = new.map { it.trackId }
                        val idsChanged = oldIds != newIds || adapter.itemCount == 0

                        val playingChanged =
                            !idsChanged &&
                                    old.size == new.size &&
                                    old.indices.any { i -> old[i].isPlaying != new[i].isPlaying }

                        // ➕ Добавили это:
                        val favoriteChanged =
                            !idsChanged && old.size == new.size &&
                                    old.indices.any { i -> old[i].isFavorite != new[i].isFavorite }

                        if (idsChanged || playingChanged || favoriteChanged) {
                            adapter.update(new.map { it.copy() })
                            binding.tracksRecyclerView.scrollToPosition(state.currentTrackIndex)
                            updateTimesForVisibleItems()
                        }

                        val desired = if (state.isHorizontal) LinearLayoutManager.HORIZONTAL else LinearLayoutManager.VERTICAL
                        if (desired != currentLayoutOrientation) {
                            currentLayoutOrientation = desired
                            setLayoutManager(desired)
                            adapter.setTimebarVertical(state.isHorizontal)// for audio time bar
                            binding.tracksRecyclerView.scrollToPosition(state.currentTrackIndex)
                            updateTimesForVisibleItems()
                        }else {
                            // Если LM не менялся, синхронизируем флаг (после возврата на экран, восстановления и т.п.)
                            adapter.setTimebarVertical(state.isHorizontal)
                        }
                        binding.tracksRecyclerView.isVisible = !state.isBottomNavVisible
                        requireActivity().findViewById<TextView>(R.id.title)?.isVisible = state.isBottomNavVisible
                        requireActivity().findViewById<Toolbar>(R.id.toolbar)?.apply {
                            val fixedHeightInPx = 45.convertDpToPx(requireContext())
                            layoutParams.height = fixedHeightInPx
                            requestLayout()
                        }

                        // audio time bar // ---- ЕДИНСТВЕННЫЙ блок для аудио-таймбара ----
                        if (videoBoundPosition == NO_VIDEO_POSITION) {
                            val pos = state.currentTrackIndex
                            // спрятать у всех видимых, кроме текущей
                            adapter.hideAudioTimebarForVisibleExcept(binding.tracksRecyclerView, pos)

                            val current = state.trackList.getOrNull(pos)
                            if (current?.isPlaying == true) {
                                // 1) двигаем ползунок
                                viewModel.getAudioProgress()?.let { p ->
                                    adapter.updateAudioTimebarAt(
                                        binding.tracksRecyclerView,
                                        pos,
                                        p.positionMs, p.durationMs, p.bufferedMs
                                    )
                                    // 2) обновляем подпись времени
                                    adapter.updatePlayTimeTextAt(
                                        binding.tracksRecyclerView,
                                        pos,
                                        formatMs(p.positionMs)
                                    )
                                }
                            } else {
                                // не играет — не двигаем ползунок на этой карточке
                                // (можно скрыть или обнулить на всякий)
                                // adapter.zeroAudioTimebarForVisibleExcept(binding.tracksRecyclerView, pos)
                                adapter.updatePlayTimeTextAt(binding.tracksRecyclerView, pos, "0:00")
                            }
                        }
                    }
                }
                launch { viewModel.playlists.collect { list -> bottomAdapter.submitList(list) } }
                launch {
                    viewModel.playlistEvents.collect { e ->
                        when (e) {
                            is ExtraOptionViewModel.PlaylistEvent.Added -> {
                                bottomBehavior.state = BottomSheetBehavior.STATE_HIDDEN
                                showSnack(getString(R.string.added_to_playlist, e.playlistName))
                            }
                            is ExtraOptionViewModel.PlaylistEvent.AlreadyExists ->
                                showSnack(getString(R.string.track_already_in_playlist, e.playlistName))
                            is ExtraOptionViewModel.PlaylistEvent.Error ->
                                showSnack(e.message)
                        }
                    }
                }
            }
        }

        // Скролл — только индекс + корректные таймеры
        binding.tracksRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                val pos = currentSnappedPosition()
                val cur = viewModel.state.value.currentTrackIndex
                if (pos != RecyclerView.NO_POSITION && pos != cur) {

                    // 1) обновляем индекс
                    viewModel.setCurrentTrackIndex(pos)
                    viewModel.setScrollPosition(pos)

                    // for audio time bar
                    // 2) если ИДЁТ АУДИО (видео нет) — спрячь/обнули таймбары у всех видимых, кроме текущей
                    if (videoBoundPosition == NO_VIDEO_POSITION) {
                        adapter.hideAudioTimebarForVisibleExcept(binding.tracksRecyclerView, pos)
                        // если вместо скрытия нужно "ноль у всех", используй:
                        // adapter.zeroAudioTimebarForVisibleExcept(binding.tracksRecyclerView, pos)
                    }
                }
                // 3) если идёт ВИДЕО — обновляем его время для видимых карточек (как было)
                if (videoBoundPosition != NO_VIDEO_POSITION) updateTimesForVisibleItems()
            }
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    val pos = currentSnappedPosition().takeIf { it != RecyclerView.NO_POSITION } ?: 0

                    // 1) фиксируем индекс
                    viewModel.setCurrentTrackIndex(pos)
                    viewModel.setScrollPosition(pos)

                    // replace by audio time bar
                    if (videoBoundPosition != NO_VIDEO_POSITION) {
                        // 2a) при видео — обновляем таймеры (как было)
                        updateTimesForVisibleItems()
                    } else {
                        // 2b) при аудио — скрыть/обнулить у всех, кроме текущей...
                        adapter.hideAudioTimebarForVisibleExcept(binding.tracksRecyclerView, pos)

                        val current = viewModel.state.value.trackList.getOrNull(pos)
                        if (current?.isPlaying == true) {
                            // ...и тут же оживить ползунок у текущей карточки актуальным прогрессом
                            viewModel.getAudioProgress()?.let { p ->
                                adapter.updateAudioTimebarAt(
                                    binding.tracksRecyclerView,
                                    pos,
                                    p.positionMs, p.durationMs, p.bufferedMs
                                )

                                // + текст времени у текущей карточки
                                adapter.updatePlayTimeTextAt(
                                    binding.tracksRecyclerView,
                                    pos,
                                    formatMs(p.positionMs)  // String
                                )
                            }
                        } else { // else — ничего не обновляем (ползунок скрыт/ноль)
                            adapter.updatePlayTimeTextAt(binding.tracksRecyclerView, pos, "0:00")
                        }
                    }
                }
            }
        })

        // init args
        if (savedInstanceState == null) {
            arguments?.let {
                val json = it.getString("TRACK_LIST_JSON") ?: return@let
                val index = it.getInt("TRACK_INDEX")
                trackListIntentParser.parse(json, index)?.let { inputData ->
                    viewModel.initializeWith(inputData)
                }
            }
        }

        // notifications permission (13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && savedInstanceState == null) {
            val hasPermission = requireContext()
                .checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
            if (!hasPermission) {
                if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                    showSnack(getString(R.string.permission_notifications_rationale))
                } else {
                    requestNotifPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        }

        binding.root.findViewById<View>(R.id.bottom6)?.isSelected = true
        viewModel.startFavoritesSyncIfNeeded()
    }

    // ───────────────── lifecycle / service ─────────────────

    override fun onStart() {
        super.onStart()
        viewModel.ensurePlayer(requireContext())
        requireContext().bindService(buildBindIntentForCurrent(), connection, Context.BIND_AUTO_CREATE)
    }

    override fun onStop() {
        super.onStop()
        val activity = requireActivity()
        val changingCfg = activity.isChangingConfigurations
        val finishing = activity.isFinishing
        val goingToBackground = !changingCfg && !finishing && !activity.hasWindowFocus()
        if (!goingToBackground) return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS) ==
                    PackageManager.PERMISSION_GRANTED
            if (!granted) return
        }

        viewModel.onUiWentBackground()
        stopVideoTicker()
    }

    override fun onDestroyView() {
        val reallyClosingScreen =
            (isRemoving && !requireActivity().isChangingConfigurations) ||
                    requireActivity().isFinishing

        if (reallyClosingScreen) {
            viewModel.stopAudioPlay()
            detachCurrentVideo()
            viewModel.releasePlayer()
        }

        super.onDestroyView()

        if (!requireActivity().isChangingConfigurations && isBound) {
            try { requireContext().unbindService(connection) } catch (_: Exception) {}
            isBound = false
            musicService = null
            viewModel.removeAudioPlayerControl()
        }
    }

    @SuppressLint("ObsoleteSdkInt")
    override fun onResume() {
        super.onResume()
        (activity as? BaseActivity)?.updateSegmentTexts()
        if (hideBottomSheetIfOpen()) { overlay.visibility = View.GONE; overlay.alpha = 0f }

        viewModel.onUiCameToForeground()
        (activity as? BaseActivity)?.applyThemeThenRestoreSaved()

        cm = requireContext().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        lastConnected = networkChecker.isNetworkAvailable()

        networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) { lastConnected = true }
            override fun onLost(network: Network) {
                val now = networkChecker.isNetworkAvailable()
                val was = lastConnected
                if (was == true && !now) showLongSnack(getString(R.string.no_internet_connection))
                lastConnected = now
            }
            override fun onCapabilitiesChanged(network: Network, caps: NetworkCapabilities) {
                val now = networkChecker.isNetworkAvailable()
                val was = lastConnected
                if (was == true && !now) showLongSnack(getString(R.string.no_internet_connection))
                lastConnected = now
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            cm?.registerDefaultNetworkCallback(networkCallback!!)
        } else {
            val req = NetworkRequest.Builder().addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET).build()
            cm?.registerNetworkCallback(req, networkCallback!!)
        }
    }

    override fun onPause() {
        super.onPause()
        val pos = (binding.tracksRecyclerView.layoutManager as? LinearLayoutManager)
            ?.findFirstVisibleItemPosition() ?: 0
        viewModel.setScrollPosition(pos)

        networkCallback?.let { cb ->
            try { cm?.unregisterNetworkCallback(cb) } catch (_: Exception) {}
        }
        networkCallback = null
        cm = null
    }

    // ───────────────── helpers ─────────────────

    fun shareSingleTrack() {
        viewModel.getCurrentTrack()?.let { shareHelper.shareTrackOrNotify(it) }
    }

    private fun setLayoutManager(orientation: Int) {
        binding.tracksRecyclerView.layoutManager = LinearLayoutManager(requireContext(), orientation, false)
        snapHelper.attachToRecyclerView(binding.tracksRecyclerView)
    }

    private fun Int.convertDpToPx(context: Context): Int =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, toFloat(), context.resources.displayMetrics).toInt()

    override fun getToolbarConfig(): ToolbarConfig =
        ToolbarConfig(View.VISIBLE, R.string.chat_btm) {
            if (viewModel.state.value.isBottomNavVisible) {
                (requireActivity() as? MainActivity)?.apply {
                    buttonIndex = -1
                    switchFragment(buttonIndex)
                    bottomNavigationHelper.selectButton(buttonIndex)
                    bottomNavigationHelper.setBottomNavigationVisibility()
                }
            } else {
                viewModel.stopAudioPlay()
                val navController = findNavController()
                val backStackEntry = try { navController.getBackStackEntry(R.id.searchFragment) } catch (_: IllegalArgumentException) { null }
                backStackEntry?.savedStateHandle?.set("from_extra", true)
                requireActivity().onBackPressedDispatcher.onBackPressed()
            }
        }

    override fun getBottomNavButtonIndex(): Int = 5
    override fun shouldShowBottomNav(): Boolean = !isFromSearch
    override fun shouldShowFullBottomNav(): Boolean = isFromSearch

    override fun onSegment4ClickedInternal() {
        triggerYouTubeSearchFromToolbar()
    }

    private fun showSnack(text: String, durationMs: Int = 4000) {
        val root = requireActivity().findViewById<View>(android.R.id.content)
        val sb = com.google.android.material.snackbar.Snackbar
            .make(root, text, com.google.android.material.snackbar.Snackbar.LENGTH_LONG)
        (sb.view.layoutParams as? ViewGroup.MarginLayoutParams)?.setMargins(0, 0, 0, 0)
        sb.duration = durationMs
        sb.show()
    }

    private fun hideBottomSheetIfOpen(): Boolean {
        if (::bottomBehavior.isInitialized && bottomBehavior.state != BottomSheetBehavior.STATE_HIDDEN) {
            bottomBehavior.state = BottomSheetBehavior.STATE_HIDDEN
            return true
        }
        return false
    }

    private fun isLegacyDevice(): Boolean {
        // Старые считаем API <= 28
        return Build.VERSION.SDK_INT <= Build.VERSION_CODES.P
    }

    private fun triggerYouTubeSearchFromToolbar() {
        val snapped = currentSnappedPosition()
        if (snapped != RecyclerView.NO_POSITION && snapped != viewModel.state.value.currentTrackIndex) {
            viewModel.setCurrentTrackIndex(snapped)
            viewModel.setScrollPosition(snapped)
        }

        val track = viewModel.getCurrentTrack() ?: run {
            showLongSnack(getString(R.string.no_track_selected))
            return
        }

        viewLifecycleOwner.lifecycleScope.launch {
            showLongSnack(getString(R.string.searching_on_youtube))

            val legacy = isLegacyDevice()

            // 1) Вызываем нужный резолвер (НЕ подавляем типы!)
            val resultLegacy: com.example.playlistmaker.presentation.utils.YoutubeDirectResolver.Result?
            val resultNew:    com.example.playlistmaker.presentation.utils.YoutubeNewPipeResolver.Result?

            if (legacy) {
                resultLegacy = com.example.playlistmaker.presentation.utils.YoutubeDirectResolver.searchBestAudio(
                    trackName = track.trackName,
                    artistName = track.artistName,
                    preferChannel = null,
                    maxResults = 5,
                    maxDurationSec = null,
                    audioOnly = true
                )
                resultNew = null
            } else {
                resultNew = com.example.playlistmaker.presentation.utils.YoutubeNewPipeResolver.searchBestAudio(
                    trackName = track.trackName,
                    artistName = track.artistName,
                    preferChannel = null,
                    maxResults = 5,
                    maxDurationSec = null
                )
                resultLegacy = null
            }

            // Проверка на null
            val hasAny = (resultLegacy != null || resultNew != null)
            if (!hasAny) {
                showLongSnack(getString(R.string.nothing_found_utube))
                return@launch
            }

            // 2) Сброс текущего аудио в UI
            viewModel.stopAudioPlay()
            val audioIndex = viewModel.state.value.currentTrackIndex
            viewModel.updatePlayingUiForIndex(audioIndex, false)
            viewModel.updatePlayTimeForIndex(audioIndex, "0:00")

            // 3) Ветка «новые девайсы с прогрессивным видео»
            val mp4: String? = resultNew?.progressiveVideoUrl // строгий тип String?
            if (!legacy && !mp4.isNullOrBlank()) {
                // видео-режим
                detachCurrentVideo()
                stopAndReleaseVideo()

                val pos = viewModel.state.value.currentTrackIndex
                val player = exo ?: run {
                    viewModel.ensurePlayer(requireContext())
                    viewModel.exo!!
                }

                // MediaItem.fromUri ожидает String или Uri — здесь точно String
                player.setMediaItem(androidx.media3.common.MediaItem.fromUri(mp4))
                player.prepare()
                player.playWhenReady = false // старт по кнопке

                attachVideoPlayerCallbacks()

                adapter.setVideoPlayer(player)
                adapter.setVideoBoundPosition(pos)
                adapter.attachVideoAt(binding.tracksRecyclerView, pos, player)
                videoBoundPosition = pos

                resetTimeForIndex(pos)
                updateTimesForVisibleItems()

                val mime: String = resultNew.progressiveMime ?: "video/mp4"
                val titleShown: String = resultNew.title
                showLongSnack(getString(R.string.youtube_progressive_found, mime, titleShown))
                return@launch
            }

            // 4) Фоллбэк: только аудио (или «старые»)
            // Достаём поля единообразно
            val audioUrl: String = resultLegacy?.audioUrl ?: resultNew?.audioUrl ?: return@launch
            val title: String = resultLegacy?.title ?: resultNew?.title ?: track.trackName
            val channel: String = resultLegacy?.channel ?: resultNew?.channel ?: track.artistName

            showLongSnack(getString(R.string.youtube_audio_only_found, title, channel))

            musicService?.apply {
                setTrack(
                    url = audioUrl,
                    trackId = track.trackId,
                    artist = channel,
                    title = title
                )
                startPlayer()
            }
        }
    }

    private fun detachCurrentVideo() {
        if (videoBoundPosition != NO_VIDEO_POSITION) {
            adapter.detachVideoAt(binding.tracksRecyclerView, videoBoundPosition)
            adapter.clearVideoBoundPosition()
            adapter.setVideoPlayer(null)
            videoBoundPosition = NO_VIDEO_POSITION
            stopVideoTicker()
            updateTimesForVisibleItems()
        }
    }

    private fun togglePlayPauseForCurrent() {
        val cur = viewModel.state.value.currentTrackIndex
        if (videoBoundPosition == cur && exo != null) {
            exo?.let { player ->
                if (player.isPlaying) {
                    player.pause()
                    stopVideoTicker()
                    viewModel.updatePlayingUiForIndex(cur, false)
                } else {
                    player.play()
                    startVideoTicker()
                    viewModel.updatePlayingUiForIndex(cur, true)
                }
            }
            return
        } else {
            val track = viewModel.getCurrentTrack() ?: return
            viewModel.audioPlay(track)
        }
    }

    private fun ExtraOptionViewModel.updatePlayingUiForIndex(index: Int, playing: Boolean) {
        updateState { s ->
            val list = s.trackList.toMutableList()
            if (index in list.indices) list[index] = list[index].copy(isPlaying = playing)
            s.copy(trackList = list)
        }
    }

    private fun ExtraOptionViewModel.updatePlayTimeForIndex(index: Int, time: String) {
        updateState { s ->
            val list = s.trackList.toMutableList()
            if (index in list.indices) list[index] = list[index].copy(playTime = time)
            s.copy(trackList = list)
        }
    }

    @SuppressLint("DefaultLocale")
    private fun startVideoTicker() {
        videoTickerJob?.cancel()
        val player = exo ?: return
        videoTickerJob = viewLifecycleOwner.lifecycleScope.launch {
            while (isActive && player.playbackState != Player.STATE_IDLE) {
                val pos = videoBoundPosition
                if (pos != NO_VIDEO_POSITION) {
                    val ms = player.currentPosition.coerceAtLeast(0L)
                    val mm = (ms / 1000 / 60).toInt()
                    val ss = ((ms / 1000) % 60).toInt()
                    val text = String.format("%d:%02d", mm, ss)
                    adapter.updatePlayTimeTextAt(binding.tracksRecyclerView, pos, text)
                }
                delay(500)
            }
        }
    }

    private fun stopVideoTicker() {
        videoTickerJob?.cancel()
        videoTickerJob = null
    }

    private fun resetTimeForIndex(index: Int) {
        if (index >= 0) {
            viewModel.updatePlayTimeForIndex(index, "0:00")
            viewModel.updatePlayingUiForIndex(index, false)
        }
    }

    private fun stopAndReleaseVideo() {
        stopVideoTicker()
        videoPlayerListener?.let { exo?.removeListener(it) }
        videoPlayerListener = null
        exo?.stop()
        exo?.clearMediaItems()
    }

    private fun currentSnappedPosition(): Int {
        val lm = binding.tracksRecyclerView.layoutManager as? LinearLayoutManager
            ?: return RecyclerView.NO_POSITION
        val snapView = snapHelper.findSnapView(lm) ?: return lm.findFirstVisibleItemPosition()
        return lm.getPosition(snapView)
    }

    // обновляем время только у видимых: у позиции с видео — текущее; у остальных — 0:00
    @SuppressLint("DefaultLocale")
    private fun updateTimesForVisibleItems() {

        // 👉 НЕТ видео — НИЧЕГО не трогаем (аудио-время рисует VM)
        if (videoBoundPosition == NO_VIDEO_POSITION || exo == null) return

        val lm = binding.tracksRecyclerView.layoutManager as? LinearLayoutManager ?: return
        val first = lm.findFirstVisibleItemPosition()
        val last = lm.findLastVisibleItemPosition()
        if (first == RecyclerView.NO_POSITION || last == RecyclerView.NO_POSITION) return

        val player = exo
        for (pos in first..last) {
            if (pos == videoBoundPosition && player != null) {
                val ms = player.currentPosition.coerceAtLeast(0L)
                val mm = (ms / 1000 / 60).toInt()
                val ss = ((ms / 1000) % 60).toInt()
                val text = String.format("%d:%02d", mm, ss)
                adapter.updatePlayTimeTextAt(binding.tracksRecyclerView, pos, text)
            } else {
                adapter.updatePlayTimeTextAt(binding.tracksRecyclerView, pos, "0:00")
            }
        }
    }

    // audio time bar
    @SuppressLint("DefaultLocale")
    private fun formatMs(ms: Long): String {
        val s = (ms.coerceAtLeast(0L) / 1000)
        val mm = s / 60
        val ss = s % 60
        return String.format("%d:%02d", mm, ss)
    }

    // stop video by own
    private fun attachVideoPlayerCallbacks() {
        val player = exo ?: return

        // снять старый, чтобы не плодить слушателей
        videoPlayerListener?.let { player.removeListener(it) }

        val l = object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                val pos = videoBoundPosition
                if (pos != NO_VIDEO_POSITION) {
                    // синхронизируем кнопку Play/Pause на карточке с нативным контроллером PlayerView
                    viewModel.updatePlayingUiForIndex(pos, isPlaying)
                }
                if (isPlaying) startVideoTicker() else stopVideoTicker()
            }

            override fun onPlaybackStateChanged(state: Int) {
                if (state == Player.STATE_ENDED) {
                    val pos = videoBoundPosition
                    // видео закончилось → кнопка должна стать "Play"
                    if (pos != NO_VIDEO_POSITION) {
                        viewModel.updatePlayingUiForIndex(pos, false)
                        // по желанию — обнулить подпись времени на карточке
                        adapter.updatePlayTimeTextAt(binding.tracksRecyclerView, pos, "0:00")
                    }
                    stopVideoTicker()
                    // вернуть в начало, чтобы следующий Play начинал с 0
                    player.seekTo(0)
                    player.playWhenReady = false
                }
            }
        }

        player.addListener(l)
        videoPlayerListener = l
    }
}
