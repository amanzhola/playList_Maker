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
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.Toolbar
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.navigation.fragment.findNavController
import com.example.playlistmaker.BaseActivity
import com.example.playlistmaker.BaseFragment
import com.example.playlistmaker.R
import com.example.playlistmaker.domain.api.base.NetworkStatusChecker
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
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class AudioPlayerFragment : BaseFragment(), BottomNavConfig {

    private var bgColorExt: androidx.compose.ui.graphics.Color? by
    androidx.compose.runtime.mutableStateOf(null)
    private var textColorExt: androidx.compose.ui.graphics.Color? by
    androidx.compose.runtime.mutableStateOf(null)
    private var iconColorExt: androidx.compose.ui.graphics.Color? by
    androidx.compose.runtime.mutableStateOf(null)

    // ───────── ticker / player listener ─────────
    private var videoTickerJob: Job? = null
    private var videoPlayerListener: Player.Listener? = null

    private fun stopVideoTicker() {
        videoTickerJob?.cancel()
        videoTickerJob = null
    }

    private fun startVideoTickerSafely() {
        viewLifecycleOwnerLiveData.value?.let { startVideoTicker(it) }
    }

    // VM / player
    private val viewModel: ExtraOptionViewModel by viewModel()
    private val exo: ExoPlayer? get() = viewModel.exo
    private var videoBoundPosition: Int
        get() = viewModel.videoPos
        set(value) { viewModel.videoPos = value }

    private val networkChecker: NetworkStatusChecker by inject { parametersOf(requireContext()) }
    private var cm: ConnectivityManager? = null
    private var networkCallback: ConnectivityManager.NetworkCallback? = null
    private var lastConnected: Boolean? = null

    private val shareHelper: AudioSingleTrackShare by inject { parametersOf(requireActivity()) }
    private val trackListIntentParser: TrackListIntentParser by inject()
    private val imageLoader: ImageLoader by inject()

    private var isFromSearch: Boolean = false

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
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val ctx = requireContext()

        // Инициализация VM из аргументов ДО setContent (как у тебя)
        if (viewModel.state.value.trackList.isEmpty()) {
            arguments?.getString("TRACK_LIST_JSON")?.let { json ->
                val index = arguments?.getInt("TRACK_INDEX") ?: 0
                trackListIntentParser.parse(json, index)?.let(viewModel::initializeWith)
            }
        }

        // 1) Корень: FrameLayout
        val root = android.widget.FrameLayout(ctx).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            id = View.generateViewId()
            setBackgroundColor(
                ContextCompat.getColor(ctx, R.color.white_textColor)
            )
        }

        // 2) Compose-контент
        val compose = ComposeView(ctx).apply {
            id = R.id.composeContent  // <-- важно для ActivityUiHider
            setBackgroundColor(android.graphics.Color.TRANSPARENT)
            setContent {
                AudioPlayerScreen(
                    viewModel = viewModel,
                    imageLoader = imageLoader,
                    onBack = {
                        detachCurrentVideo()
                        stopAndReleaseVideo()
                        viewModel.stopAudioPlay()
                        requireActivity().onBackPressedDispatcher.onBackPressed()
                    },
                    onCreatePlaylistClick = {
                        findNavController().navigate(R.id.action_global_to_createPlaylistFragment)
                    },
                    onTogglePlayPauseForCurrent = { togglePlayPauseForCurrent() },
                    onSeek = { ms ->
                        if (viewModel.videoPos == NO_VIDEO_POSITION) viewModel.seekTo(ms)
                    },
                    extBackground = bgColorExt,
                    extTextColor  = textColorExt,
                    extIconTint   = iconColorExt
                )
            }
        }

        // 3) fail.xml поверх (по умолчанию у него visibility=gone через стиль)
        val failView = (layoutInflater.inflate(R.layout.fail, root, false) as TextView).apply {
            id = R.id.failText   // <-- важно для ActivityUiHider и общего поиска
            // Позиционирование аналогично Search: TOP + CENTER_HORIZONTAL
            layoutParams = android.widget.FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = android.view.Gravity.TOP or android.view.Gravity.CENTER_HORIZONTAL
                // если надо опустить «под тулбар/хедер», подбери димен
                topMargin = resources.getDimensionPixelSize(R.dimen.track_45)
            }
        }

        root.addView(compose)
        root.addView(failView)

        // ⛑ восстановление коллбеков видео после пересоздания view (смена темы и т.п.)
        if (viewModel.videoPos != NO_VIDEO_POSITION && viewModel.exo != null) {
            attachVideoPlayerCallbacks()
            if (viewModel.exo?.isPlaying == true) {
                startVideoTicker(viewLifecycleOwner)
            }
        }

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // one-shot toast после создания плейлиста
        val handle = findNavController().currentBackStackEntry?.savedStateHandle
        handle?.getLiveData<String>(NavKeys.PLAYLIST_CREATED_NAME)
            ?.observe(viewLifecycleOwner) { name ->
                showSnack(getString(R.string.playlist_created, name), durationMs = 4000)
                handle.remove<String>(NavKeys.PLAYLIST_CREATED_NAME)
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

        // Подписки, которые остаются (без каких-либо ссылок на удалённый XML)
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                // управление тулбаром/нижней навигацией по состоянию
                launch {
                    viewModel.state.collect { state ->
                        requireActivity().findViewById<TextView>(R.id.title)?.isVisible =
                            state.isBottomNavVisible
                        requireActivity().findViewById<Toolbar>(R.id.toolbar)?.apply {
                            val fixedHeightInPx = 45.convertDpToPx(requireContext())
                            layoutParams.height = fixedHeightInPx
                            requestLayout()
                        }
                    }
                }
                // как только сервис играет аудио — отвязываем локальное видео
                launch {
                    viewModel.state.collect { st ->
                        if (st.playbackState == com.example.playlistmaker.domain.api.player.PlaybackState.PLAYING) {
                            if (videoBoundPosition != NO_VIDEO_POSITION) {
                                detachCurrentVideo()
                                stopAndReleaseVideo()
                                viewModel.updatePlayingUiForIndex(st.currentTrackIndex, false)
                            }
                        }
                    }
                }
            }
        }

        viewModel.startFavoritesSyncIfNeeded()
    }


    // ───────── lifecycle / service ─────────

    override fun onStart() {
        super.onStart()
        viewModel.ensurePlayer()
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
            val granted = ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) return
        }

        viewModel.onUiWentBackground()
        stopVideoTicker()
    }

    override fun onDestroyView() {
        restoreToolbarDefaultsAfterAudio()
        exo?.let { p -> videoPlayerListener?.let { p.removeListener(it) } }
        videoPlayerListener = null
        stopVideoTicker()

        val reallyClosingScreen =
            (isRemoving && !requireActivity().isChangingConfigurations) || requireActivity().isFinishing

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

        android.util.Log.d("TITLEFLOW","Audio.onResume()")
        (activity as? BaseActivity)?.toolbarHelper?.dumpState()
        (activity as? BaseActivity)?.updateSegmentTexts()

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
        android.util.Log.d("TITLEFLOW","Audio.onPause()")
        viewModel.setScrollPosition(viewModel.state.value.currentTrackIndex)

        networkCallback?.let { cb ->
            try { cm?.unregisterNetworkCallback(cb) } catch (_: Exception) {}
        }
        networkCallback = null
        cm = null
    }

    // ───────── helpers ─────────

    fun shareSingleTrack() {
        viewModel.getCurrentTrack()?.let { shareHelper.shareTrackOrNotify(it) }
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

    override fun getBottomNavButtonIndex(): Int? = null
    override fun shouldShowBottomNav(): Boolean = false
    override fun shouldShowFullBottomNav(): Boolean = false

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

    private fun isLegacyDevice(): Boolean {
        // Старые считаем API <= 28
        return Build.VERSION.SDK_INT <= Build.VERSION_CODES.P
    }

    private fun triggerYouTubeSearchFromToolbar() {
        val track = viewModel.getCurrentTrack() ?: run {
            showLongSnack(getString(R.string.no_track_selected))
            return
        }

        viewLifecycleOwner.lifecycleScope.launch {
            showLongSnack(getString(R.string.searching_on_youtube))

            val legacy = isLegacyDevice()

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

            // 3) Видео (привязка к текущей странице)
            val mp4: String? = resultNew?.progressiveVideoUrl
            if (!legacy && !mp4.isNullOrBlank()) {
                detachCurrentVideo()
                stopAndReleaseVideo()

                val pos = viewModel.state.value.currentTrackIndex
                val player = exo ?: run {
                    viewModel.ensurePlayer()
                    viewModel.exo!!
                }

                player.setMediaItem(androidx.media3.common.MediaItem.fromUri(mp4))
                player.prepare()
                player.playWhenReady = false

                attachVideoPlayerCallbacks()
                videoBoundPosition = pos
                resetTimeForIndex(pos)

                val mime: String = resultNew.progressiveMime ?: "video/mp4"
                val titleShown: String = resultNew.title
                showLongSnack(getString(R.string.youtube_progressive_found, mime, titleShown))
                return@launch
            }

            // 4) Фоллбэк: только аудио
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
            videoBoundPosition = NO_VIDEO_POSITION
            stopVideoTicker()
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
                    startVideoTickerSafely()
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
    private fun startVideoTicker(owner: LifecycleOwner) {
        videoTickerJob?.cancel()
        val player = exo ?: return

        videoTickerJob = owner.lifecycleScope.launch {
            while (isActive && player.playbackState != Player.STATE_IDLE) {
                val pos = videoBoundPosition
                if (pos != NO_VIDEO_POSITION) {
                    val ms = player.currentPosition.coerceAtLeast(0L)
                    viewModel.updatePlayTimeForIndex(pos, formatMs(ms))
                }
                delay(500)
            }
        }
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

    @SuppressLint("DefaultLocale")
    private fun formatMs(ms: Long): String {
        val s = (ms.coerceAtLeast(0L) / 1000)
        val mm = s / 60
        val ss = s % 60
        return String.format("%d:%02d", mm, ss)
    }

    private fun attachVideoPlayerCallbacks() {
        val player = exo ?: return
        videoPlayerListener?.let { player.removeListener(it) }

        val l = object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                val pos = videoBoundPosition
                if (pos != NO_VIDEO_POSITION) {
                    viewModel.updatePlayingUiForIndex(pos, isPlaying)
                }
                if (isPlaying) startVideoTickerSafely() else stopVideoTicker()
            }

            override fun onPlaybackStateChanged(state: Int) {
                if (state == Player.STATE_ENDED) {
                    val pos = videoBoundPosition
                    if (pos != NO_VIDEO_POSITION) {
                        viewModel.updatePlayingUiForIndex(pos, false)
                        viewModel.updatePlayTimeForIndex(pos, "0:00")
                    }
                    stopVideoTicker()
                    player.seekTo(0)
                    player.playWhenReady = false
                }
            }
        }

        player.addListener(l)
        videoPlayerListener = l
    }

    private fun restoreToolbarDefaultsAfterAudio() {
        val tb = requireActivity().findViewById<Toolbar>(R.id.toolbar) ?: return
        tb.findViewById<TextView>(R.id.title)?.isVisible = true
        tb.layoutParams = tb.layoutParams.apply { height = ViewGroup.LayoutParams.WRAP_CONTENT }
        tb.requestLayout()
        (activity as? BaseActivity)?.applyToolbarThemeColors()
    }

    fun setAudioBackgroundColor(@androidx.annotation.ColorInt color: Int) {
        bgColorExt = androidx.compose.ui.graphics.Color(color)
    }
    fun setAudioTextColor(@androidx.annotation.ColorInt color: Int) {
        textColorExt = androidx.compose.ui.graphics.Color(color)
    }
    fun setAudioIconColor(@androidx.annotation.ColorInt color: Int) {
        iconColorExt = androidx.compose.ui.graphics.Color(color)
    }
}
