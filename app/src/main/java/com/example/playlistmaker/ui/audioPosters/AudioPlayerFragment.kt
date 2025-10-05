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
import com.example.playlistmaker.utils.NavKeys
import com.example.playlistmaker.utils.showLongSnack
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class AudioPlayerFragment : BaseFragment(), BottomNavConfig {

    // ─────────────────────────────────────────────────────────────────────────────
    // [ADDED] Запрос Разрешения
    // ─────────────────────────────────────────────────────────────────────────────
    // Современный запрос разрешения (Android 13+)
    private val requestNotifPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                // ✅ Разрешение выдано — показывать уведомления во время PLAYING
                showSnack(getString(R.string.permission_notifications_granted))
            } else {
                // ❌ Отказ — покажи краткое пояснение
                // Если юзер нажал «Не спрашивать снова», стоит предложить открыть настройки
                showSnack(getString(R.string.permission_notifications_denied))
            }
        }

    // use ConnectivityManager.NetworkCallback instead BroadcastReceiver cause depreciated CONNECTIVITY_ACTION
    private val networkChecker: NetworkStatusChecker by inject { parametersOf(requireContext()) }
    private var cm: ConnectivityManager? = null
    private var networkCallback: ConnectivityManager.NetworkCallback? = null
    private var lastConnected: Boolean? = null

    private lateinit var binding: FragmentExtraOptionBinding
    private lateinit var adapter: TrackAdapterAudio
    private val viewModel: ExtraOptionViewModel by viewModel()
    private lateinit var snapHelper: PagerSnapHelper

    private val shareHelper: AudioSingleTrackShare by inject { parametersOf(requireActivity()) }
    private val trackListIntentParser: TrackListIntentParser by inject()

    private var currentLayoutOrientation = LinearLayoutManager.HORIZONTAL
    private var isFromSearch: Boolean = false

    private lateinit var bottomBehavior: BottomSheetBehavior<LinearLayout>
    private lateinit var overlay: View
    private val imageLoader: ImageLoader by inject()
    private val bottomAdapter by lazy { PlaylistBottomAdapter(imageLoader) { viewModel.onPlaylistClicked(it) } }

    // ─────────────────────────────────────────────────────────────────────────────
    // [ADDED] Привязка к сервису AudioPlayerControl (MusicService)
    // ─────────────────────────────────────────────────────────────────────────────
    private var musicService: MusicService? = null
    private var isBound: Boolean = false

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            // [ADDED] получаем экземпляр сервиса и отдаём в VM
            val binder = service as? MusicService.MusicServiceBinder ?: return
            musicService = binder.getService()
            isBound = true
            // передаём control в VM (замена старого startObservingAudioPlayer)
            viewModel.setAudioPlayerControl(musicService!!)
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            // [ADDED] сервис умер/отключился → сообщаем VM
            isBound = false
            musicService = null
            viewModel.removeAudioPlayerControl()
        }
    }

    // [ADDED] Удобный хелпер: формируем Intent для bindService и кладём данные трека (artist/title)
    private fun buildBindIntentForCurrent(): Intent {
        val ctx = requireContext()
        val intent = Intent(ctx, MusicService::class.java)

        // По чек-листу: при привязке передаём url/artist/title/id — для текста уведомления и т.п.
        viewModel.getCurrentTrack()?.let { t ->
            intent.putExtra(EXTRA_URL, t.previewUrl)
            intent.putExtra(EXTRA_ARTIST, t.artistName)
            intent.putExtra(EXTRA_TITLE, t.trackName)
            intent.putExtra(EXTRA_ID, t.trackId)
        }
        return intent
    }
    // ─────────────────────────────────────────────────────────────────────────────

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isFromSearch = arguments?.getBoolean("IS_FROM_SEARCH", false) ?: false
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentExtraOptionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val bottom = view.findViewById<LinearLayout>(R.id.playlists_bottom_sheet)
        overlay = view.findViewById(R.id.overlay)

        // список в шторке
        val rv = view.findViewById<RecyclerView>(R.id.rvBottomPlaylists)
        rv.layoutManager = LinearLayoutManager(requireContext())
        rv.adapter = bottomAdapter // ← твой адаптер PlaylistBottomAdapter

        // ⬇️ Автоскролл к началу при вставке нового плейлиста в позицию 0
        // наблюдатель адаптера:Автоскролл к началу, когда в список прилетел новый элемент сверху
        bottomAdapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                // если вставка в начало — пролистываем к началу
                if (positionStart == 0) {
                    rv.post { rv.scrollToPosition(0) }
                }
            }
        })

        // «Новый плейлист»
        view.findViewById<View>(R.id.btnUpdate).setOnClickListener {
            bottomBehavior.state = BottomSheetBehavior.STATE_HIDDEN
            findNavController().navigate(R.id.action_global_to_createPlaylistFragment)
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
                        overlay.alpha = 0.6f      // ✨ полупрозрачное затемнение на пол-экрана
                    }
                    BottomSheetBehavior.STATE_EXPANDED -> {
                        overlay.isVisible = true
                        overlay.alpha = 1f        // максимум при полном развороте
                    }

                    BottomSheetBehavior.STATE_DRAGGING,
                    BottomSheetBehavior.STATE_SETTLING -> {
                        // no-op: эти стейты кратковременные, альфой рулит onSlide()
                    }
                }
            }

            override fun onSlide(sheet: View, slideOffset: Float) {
                // плавная анимация: 0..1 → 0..0.6 (для half), 0..1 (для expanded)
                val t = slideOffset.coerceIn(0f, 1f)
                // по желанию максимум 0.6 даже при expanded, умножай на 0.6f
                overlay.alpha = t.coerceAtMost(1f)
                overlay.isVisible = t > 0f
            }
        })

        // Поймаем одноразовое событие от CreatePlaylistFragment
        val handle = findNavController().currentBackStackEntry?.savedStateHandle
        handle?.getLiveData<String>(NavKeys.PLAYLIST_CREATED_NAME)
            ?.observe(viewLifecycleOwner) { name ->
                // На всякий случай — если шторка открыта, спрячем
                if (::bottomBehavior.isInitialized) {
                    bottomBehavior.state = BottomSheetBehavior.STATE_HIDDEN
                    overlay.visibility = View.GONE
                    overlay.alpha = 0f
                }
                showSnack(getString(R.string.playlist_created, name), durationMs = 4000)
                handle.remove<String>(NavKeys.PLAYLIST_CREATED_NAME) // очистить, чтобы не повторялось
            }

        // 🎧 Адаптер
        adapter = TrackAdapterAudio(emptyList(), object : OnTrackAudioClickListener {
            override fun onTrackClicked(track: Track, position: Int) {
                viewModel.setCurrentTrackIndex(position)
                viewModel.toggleIsHorizontal()
                viewModel.setScrollPosition(position)
            }
            override fun onPlayButtonClicked(track: Track) {
                viewModel.audioPlay(track)
            }
            override fun onBackArrowClicked() {
                viewModel.stopAudioPlay()
                requireActivity().onBackPressedDispatcher.onBackPressed()
            }
            // ❤️ Избранное
            override fun onFavoriteClicked(track: Track) {
                viewModel.onFavoriteClicked(track.id)
            }

            // 🎵➕ Add Track 👉💿
            override fun onAddTrackClicked(track: Track) {
//                viewModel.onOpenBottomSheet()                   // попросим актуальные данные (если надо)
                bottom.post {                      // чтобы не спорить с лайаутом
                    bottomBehavior.isFitToContents = false          // разрешаем половинчатое состояние
                    bottomBehavior.halfExpandedRatio = 0.6f         // половина экрана (0f..1f)
                    bottomBehavior.skipCollapsed = false            // при свайпе вниз можно вернуться в collapsed/скрыть
                    bottomBehavior.state = BottomSheetBehavior.STATE_HALF_EXPANDED
                }
            }
        })

        // ♻️ RecyclerView
        binding.tracksRecyclerView.adapter = adapter
        snapHelper = PagerSnapHelper().also { it.attachToRecyclerView(binding.tracksRecyclerView) }
        setLayoutManager(currentLayoutOrientation)

        // 👀 Подписка на состояние ViewModel (StateFlow)
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {

                // [REMOVED → REPLACED]
                // запускаем наблюдение за плеером, когда экран на виду
                // в VM есть защита от повторного старта
//                viewModel.startObservingAudioPlayer()
                // Теперь подписки активируются из viewModel.setAudioPlayerControl() после bindService.

                // 1) Состояние аудиоплеера/экрана
                launch {
                    viewModel.state.collect { state ->
                        // 🔄 Обновление списка треков
                        if (adapter.getItems() != state.trackList) {
                            adapter.update(state.trackList.map { it.copy() })
                            binding.tracksRecyclerView.scrollToPosition(state.currentTrackIndex)
                        }

                        // ↔️ Переключение ориентации (гориз/верт)
                        val desired = if (state.isHorizontal)
                            LinearLayoutManager.HORIZONTAL else LinearLayoutManager.VERTICAL
                        if (desired != currentLayoutOrientation) {
                            currentLayoutOrientation = desired
                            setLayoutManager(desired)
                            binding.tracksRecyclerView.scrollToPosition(state.currentTrackIndex)
                        }

                        // 👁️ Видимость и фиксы тулбара
                        binding.tracksRecyclerView.isVisible = !state.isBottomNavVisible
                        requireActivity().findViewById<TextView>(R.id.title)?.isVisible = state.isBottomNavVisible

                        // 🪜 Фикс высоты тулбара
                        requireActivity().findViewById<Toolbar>(R.id.toolbar)?.apply {
                            val fixedHeightInPx = 45.convertDpToPx(requireContext())
                            layoutParams.height = fixedHeightInPx
                            requestLayout()
                        }
                    }
                }

                // 2) Список плейлистов для BottomSheet
                launch {
                    viewModel.playlists.collect { list ->
                        bottomAdapter.submitList(list)
                        // (опц.) показать заглушку, если нужно:
                        // binding.emptyBottomView.isVisible = list.isEmpty()
                    }
                }

                // (опц.) 3) События добавления трека в плейлист (Toast и т.п.)
                launch {
                    viewModel.playlistEvents.collect { e ->
                        when (e) {
                            is ExtraOptionViewModel.PlaylistEvent.Added -> {
                                bottomBehavior.state = BottomSheetBehavior.STATE_HIDDEN
                                showSnack(getString(R.string.added_to_playlist, e.playlistName))
                            }
                            is ExtraOptionViewModel.PlaylistEvent.AlreadyExists -> {
                                // шторку НЕ прячем — пусть юзер выберет другой плейлист
                                showSnack(getString(R.string.track_already_in_playlist, e.playlistName))
                            }
                            is ExtraOptionViewModel.PlaylistEvent.Error -> {
                                showSnack(e.message)
                            }
                        }
                    }
                }
            }
        }

        // 🧲 Следим за скроллом — обновляем индекс текущего трека
        binding.tracksRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    val pos = (recyclerView.layoutManager as? LinearLayoutManager)
                        ?.findFirstVisibleItemPosition() ?: 0
                    viewModel.setCurrentTrackIndex(pos)
                    viewModel.setScrollPosition(pos)
                }
            }
        })

        // 🧠 Аргументы при первом запуске
        if (savedInstanceState == null) {
            arguments?.let {
                val json = it.getString("TRACK_LIST_JSON") ?: return@let
                val index = it.getInt("TRACK_INDEX")
                trackListIntentParser.parse(json, index)?.let { inputData ->
                    viewModel.initializeWith(inputData)
                }
            }
        }

        // 🔔 Запрос разрешения на уведомления (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && savedInstanceState == null) {
            val hasPermission = requireContext()
                .checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED

            if (!hasPermission) {
                val shouldExplain = shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)
                if (shouldExplain) {
                    // Коротко объясняем пользователю «зачем»
                    showSnack(getString(R.string.permission_notifications_rationale))
                    // Можно подождать 0.5–1с или показать Snackbar с action «Разрешить»
                    // и в action вызвать:
//                     requestNotifPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
                } else {
                    // Первый запрос или пользователь не запретил «навсегда» → просто спрашиваем
                    requestNotifPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        }


        // ⭐ Подсветка иконки нижнего меню
        binding.root.findViewById<View>(R.id.bottom6)?.isSelected = true

        // ❤️ один раз включаем «живую» синхронизацию флагов из БД
        viewModel.startFavoritesSyncIfNeeded()
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // [ADDED] Привязка/отвязка сервиса, + нотификация при уходе в фон/возврате
    // ─────────────────────────────────────────────────────────────────────────────

    override fun onStart() {
        super.onStart()
        // [ADDED] При входе на экран — привязываемся к сервису.
        // В Intent кладём текущий трек, чтобы сервис знал artist/title для уведомления.
        requireContext().bindService(
            buildBindIntentForCurrent(),
            connection,
            Context.BIND_AUTO_CREATE
        )
        // ok = true — система приняла запрос на bind. Фактическое соединение придёт в onServiceConnected.
        // (на случай редких fail можно логировать ok)
    }

    override fun onStop() {
        super.onStop()

        val activity = requireActivity()
        val changingCfg = activity.isChangingConfigurations
        val finishing = activity.isFinishing

        // Это «уходим в фон», если НЕ идёт конфигурационное изменение и активити не финишится,
        // и действительно теряем фокус окна (сворачивание / переключение в другое приложение)
        val goingToBackground = !changingCfg && !finishing && !activity.hasWindowFocus()
        if (!goingToBackground) return

        // Android 13+: уведомления только при наличии разрешения
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) return
        }

        // Только теперь просим VM показать foreground-уведомление (если реально PLAYING)
        viewModel.onUiWentBackground()
    }

    // Отвязка здесь:
    // [ADDED] Отвязка сервиса по условиям задачи (экран закрыт/уходит — не держим лишних связей)
    override fun onDestroyView() {
        // останавливаем трек ТОЛЬКО при реальном закрытии экрана (назад/уход со страницы),
        // но НЕ при конфигурационных изменениях и не когда Activity просто пересоздаётся из-за темы
        val reallyClosingScreen =
            (isRemoving && !requireActivity().isChangingConfigurations) ||
                    requireActivity().isFinishing

        if (reallyClosingScreen) {
            viewModel.stopAudioPlay() // требование пункта 2 — стоп при закрытии экрана/приложения
        }

        super.onDestroyView()

        // Разрываем связь с сервисом только если это НЕ конфигурационное изменение
        if (!requireActivity().isChangingConfigurations && isBound) {
            try { requireContext().unbindService(connection) } catch (_: Exception) {}
            isBound = false
            musicService = null
            viewModel.removeAudioPlayerControl()
        }
    }
    // ─────────────────────────────────────────────────────────────────────────────

    // toolbar save and apply background color
    @SuppressLint("ObsoleteSdkInt")
    override fun onResume() {
        super.onResume()
        (activity as? BaseActivity)?.updateSegmentTexts()
        if (hideBottomSheetIfOpen()) {
            overlay.visibility = View.GONE
            overlay.alpha = 0f
        }

        // [ADDED] UI вернулся на экран — просим VM скрыть уведомление
        viewModel.onUiCameToForeground()

        // fixing theme on emulator and real mobile difference
        (activity as? BaseActivity)?.applyThemeThenRestoreSaved()

        // use ConnectivityManager.NetworkCallback instead BroadcastReceiver cause depreciated CONNECTIVITY_ACTION
        cm = requireContext().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        // инициализируем предыдущее состояние (чтобы не спамить первым событием)
        lastConnected = networkChecker.isNetworkAvailable()

        networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                // стало доступно — просто запомним
                lastConnected = true
            }

            override fun onLost(network: Network) {
                // сеть потеряна → проверим реальную доступность и покажем snack при переходе true -> false
                val now = networkChecker.isNetworkAvailable()
                val was = lastConnected
                if (was == true && !now) {
                    // см. пункт 2 — используем ваш showLongSnack()
                    showLongSnack(getString(R.string.no_internet_connection))
                }
                lastConnected = now
            }

            override fun onCapabilitiesChanged(network: Network, caps: NetworkCapabilities) {
                // на некоторых устройствах потеря валидированного интернета прилетает сюда
                val now = networkChecker.isNetworkAvailable()
                val was = lastConnected
                if (was == true && !now) {
                    showLongSnack(getString(R.string.no_internet_connection))
                }
                lastConnected = now
            }
        }

        // РЕГИСТРАЦИЯ
        // API 24+ — можно коротко:
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            cm?.registerDefaultNetworkCallback(networkCallback!!)
        } else {
            // API 21–23 — явно строим запрос на интернет
            val req = NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build()
            cm?.registerNetworkCallback(req, networkCallback!!)
        }
    }

    override fun onPause() {
        super.onPause()
        val pos = (binding.tracksRecyclerView.layoutManager as? LinearLayoutManager)
            ?.findFirstVisibleItemPosition() ?: 0
        viewModel.setScrollPosition(pos)

        // use ConnectivityManager.NetworkCallback instead BroadcastReceiver cause depreciated CONNECTIVITY_ACTION
        networkCallback?.let { cb ->
            try { cm?.unregisterNetworkCallback(cb) } catch (_: Exception) {}
        }
        networkCallback = null
        cm = null
    }

    fun shareSingleTrack() {
        viewModel.getCurrentTrack()?.let { shareHelper.shareTrackOrNotify(it) }
    }

    private fun setLayoutManager(orientation: Int) {
        binding.tracksRecyclerView.layoutManager =
            LinearLayoutManager(requireContext(), orientation, false)
        snapHelper.attachToRecyclerView(binding.tracksRecyclerView)
    }

    private fun Int.convertDpToPx(context: Context): Int =
        TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            toFloat(),
            context.resources.displayMetrics
        ).toInt()

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

                // Проверяем, есть ли в back stack searchFragment
                val backStackEntry = try {
                    navController.getBackStackEntry(R.id.searchFragment)
                } catch (_: IllegalArgumentException) {
                    null
                }
                backStackEntry?.savedStateHandle?.set("from_extra", true)

                requireActivity().onBackPressedDispatcher.onBackPressed()
            }
        }

    override fun getBottomNavButtonIndex(): Int = 5
    override fun shouldShowBottomNav(): Boolean = !isFromSearch
    override fun shouldShowFullBottomNav(): Boolean = isFromSearch

    override fun onSegment4ClickedInternal() {
        viewModel.updateState { s -> s.copy(isBottomNavVisible = !s.isBottomNavVisible) }
    }

    private fun showSnack(text: String, durationMs: Int = 4000) {
        val root = requireActivity().findViewById<View>(android.R.id.content)
        val sb = com.google.android.material.snackbar.Snackbar
            .make(root, text, com.google.android.material.snackbar.Snackbar.LENGTH_LONG)

        // на всю ширину, без якоря
        (sb.view.layoutParams as? ViewGroup.MarginLayoutParams)?.setMargins(0, 0, 0, 0)

        sb.duration = durationMs
        sb.show()
    }

    private fun hideBottomSheetIfOpen(): Boolean {
        if (::bottomBehavior.isInitialized &&
            bottomBehavior.state != BottomSheetBehavior.STATE_HIDDEN) {
            bottomBehavior.state = BottomSheetBehavior.STATE_HIDDEN
            return true
        }
        return false
    }
}
