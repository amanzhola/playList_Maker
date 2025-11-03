package com.example.playlistmaker.ui.media

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.playlistmaker.BaseActivity
import com.example.playlistmaker.BaseFragment
import com.example.playlistmaker.R
import com.example.playlistmaker.presentation.utils.ToolbarConfig
import com.example.playlistmaker.roots.main.MainActivity
import com.example.playlistmaker.ui.main.BottomNavConfig
import com.example.playlistmaker.utils.NavKeys
import com.example.playlistmaker.utils.SCOPE_FAV
import com.example.playlistmaker.utils.SCOPE_PL
import com.example.playlistmaker.utils.STATE_MEDIA_TAB
import com.example.playlistmaker.utils.showLongSnack
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch

class MediaLibraryFragment :
    BaseFragment(),
    BottomNavConfig,
    MediaActiveTabProvider {

    // флаги навигации
    private var pendingTabRestore: Int? = null
    private var pendingScrollTop = false
    private var pendingRestorePlaylistId: Long? = null

    // каналы прокрутки для Playlists (compose-вкладка)
    private val playlistsScrollToId = MutableSharedFlow<Long>(replay = 1, extraBufferCapacity = 1)
    private val playlistsScrollTop  = MutableSharedFlow<Unit>(replay = 1, extraBufferCapacity = 1)

    // текущий таб
    private var currentTab = 0
    override fun currentMediaTab(): Int = currentTab

    // цвета-оверрайды (как было)
    private var favScreenBg: Color? by mutableStateOf(null)
    private var favRowText: Color? by mutableStateOf(null)
    private var favRowIcon: Color? by mutableStateOf(null)
    private var favRowBg: Color? by mutableStateOf(null)

    private var playlistsScreenBg: Color? by mutableStateOf(null)
    private var playlistsCardText: Color? by mutableStateOf(null)

    private var isBottomNavVisible = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        pendingTabRestore = savedInstanceState?.getInt(STATE_MEDIA_TAB)

        arguments?.getInt(NavKeys.SELECT_TAB, -1)?.let { sel ->
            if (sel in 0..1) {
                pendingTabRestore = sel
                arguments?.remove(NavKeys.SELECT_TAB)
            }
        }
        if (arguments?.getBoolean(NavKeys.SCROLL_TOP) == true) {
            pendingScrollTop = true
            arguments?.remove(NavKeys.SCROLL_TOP)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(STATE_MEDIA_TAB, currentTab)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val ctx = requireContext()

        // 1) Корень
        val root = FrameLayout(ctx).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            id = View.generateViewId()
            setBackgroundColor(ContextCompat.getColor(ctx, R.color.white_textColor))
        }

        // 2) Compose-контент (ВАЖНО: id = composeContent, как в Settings/Search)
        val compose = ComposeView(ctx).apply {
            id = R.id.composeContent // важно для ActivityUiHider
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                MediaLibraryScreen(
                    // стартовый таб
                    startPage = (pendingTabRestore ?: 0).coerceIn(0, 1),

                    // колбэк смены страницы → применяем тему и «текущий таб»
                    onPageChanged = { page ->
                        currentTab = page
                        val act = activity as? BaseActivity
                        val def = ContextCompat.getColor(ctx, R.color.white_textColor)
                        val scope = if (page == 0) SCOPE_FAV else SCOPE_PL
                        act?.colorPersistenceHelper?.load(scope, 1)?.let(::setActiveBackground)
                            ?: setActiveBackground(def)
                        act?.colorPersistenceHelper?.load(scope, 2)?.let(::setActiveTextColor)
                        act?.colorPersistenceHelper?.load(scope, 3)?.let(::setActiveIconColor)
                        act?.applyToolbarThemeColors()
                    },

                    // навигация
                    onOpenPlaylist = { playlistId ->
                        findNavController().navigate(
                            R.id.action_global_to_playlistInfoFragment,
                            Bundle().apply { putLong("playlistId", playlistId) }
                        )
                    },
                    onOpenTrack = { track, json, index ->
                        findNavController().navigate(
                            R.id.action_global_to_extraOptionFragment,
                            Bundle().apply {
                                putString("TRACK_LIST_JSON", json)
                                putInt("TRACK_INDEX", index)
                            }
                        )
                    },
                    onCreatePlaylist = {
                        findNavController().navigate(
                            R.id.action_global_to_createPlaylistFragment,
                            Bundle().apply { putBoolean("from_playlist", true) }
                        )
                    },

                    // цвета от внешнего хелпера
                    favScreenBackgroundOverride = favScreenBg,
                    favRowTextColorOverride     = favRowText,
                    favRowIconColorOverride     = favRowIcon,
                    favRowBackgroundOverride    = favRowBg,
                    playlistsScreenBackgroundOverride = playlistsScreenBg,
                    playlistsCardTextColorOverride    = playlistsCardText,

                    // важное: потоки управления прокруткой Playlists
                    playlistsScrollToId = playlistsScrollToId.asSharedFlow(),
                    playlistsScrollTop  = playlistsScrollTop.asSharedFlow()
                )

                // после первого прогона — стартовые флаги из аргументов гасим (чтобы не переигрывать)
                LaunchedEffect(Unit) {
                    pendingTabRestore = null
                    // если пришёл SCROLL_TOP — отправим сигнал в Playlists
                    if (pendingScrollTop) {
                        playlistsScrollTop.tryEmit(Unit)
                        pendingScrollTop = false
                    }
                }
            }
        }
        // 3) Поверх — тот же fail, что в Settings
        val failView = (inflater.inflate(R.layout.fail, root, false) as TextView).apply {
            // если у fail.xml нет id=failText — подстрахуемся
            if (id == View.NO_ID) id = R.id.failText
            layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = android.view.Gravity.TOP or android.view.Gravity.CENTER_HORIZONTAL
                topMargin = resources.getDimensionPixelSize(R.dimen.track_45)
            }
            visibility = View.GONE
            isClickable = true
            isFocusable = true
        }
        root.addView(compose)
        root.addView(failView)
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        android.util.Log.d("TITLEFLOW","Media.onCreateView setContent()")

        // snackbar «плейлист создан»
        val entry = findNavController().currentBackStackEntry ?: return
        val flow = entry.savedStateHandle.getStateFlow<String?>(NavKeys.PLAYLIST_CREATED_NAME, null)
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                flow.filterNotNull().collect { name ->
                    showLongSnack(getString(R.string.playlist_created, name), durationMs = 4000)
                    entry.savedStateHandle.remove<String>(NavKeys.PLAYLIST_CREATED_NAME)
                }
            }
        }

        // слушаем свой backStackEntry (для switch табов/восстановления позиции)
        val mediaEntry = findNavController().getBackStackEntry(R.id.mediaLibraryFragment)

        // 1) просто переключить вкладку
        mediaEntry.savedStateHandle.getLiveData<Int>(NavKeys.SELECT_TAB)
            .observe(viewLifecycleOwner) { idx ->
                if (idx == 1) {
                    // пусть сам Compose-пейджер подхватит: прокинем событие top или id ниже
                    currentTab = 1
                } else if (idx == 0) {
                    currentTab = 0
                }
                mediaEntry.savedStateHandle.remove<Int>(NavKeys.SELECT_TAB)
            }

        // 2) вернуться с Info → НЕ scroll top; просто прокрутиться к нужному id
        mediaEntry.savedStateHandle.getLiveData<Long>(NavKeys.RESTORE_PLAYLIST_ID)
            .observe(viewLifecycleOwner) { restoreId ->
                if (restoreId != null) {
                    // переключаемся на таб 1 и отправляем id в поток
                    currentTab = 1
                    playlistsScrollToId.tryEmit(restoreId)
                    mediaEntry.savedStateHandle.remove<Long>(NavKeys.RESTORE_PLAYLIST_ID)
                }
            }
    }

    // --- BottomNav ---
    override fun getBottomNavButtonIndex(): Int = 1
    override fun shouldShowFullBottomNav(): Boolean = false
    override fun shouldShowBottomNav(): Boolean = true

    override fun onSegment4ClickedInternal() {
        if (isBottomNavVisible) getBaseActivity()?.hideBottomNavigation()
        else getBaseActivity()?.showBottomNavigation()
        isBottomNavVisible = !isBottomNavVisible
    }

    override fun getToolbarConfig(): ToolbarConfig =
        ToolbarConfig(View.GONE, R.string.media) {
            (requireActivity() as? MainActivity)?.apply {
                buttonIndex = -1
                switchFragment(buttonIndex)
                bottomNavigationHelper.selectButton(buttonIndex)
                bottomNavigationHelper.setBottomNavigationVisibility()
            }
        }

    /* ========= маршрутизаторы под активный таб ========= */
    fun setActiveBackground(c: Int) { if (currentTab == 0) favScreenBg = Color(c) else playlistsScreenBg = Color(c) }
    fun setActiveTextColor(c: Int)   { if (currentTab == 0) favRowText  = Color(c) else playlistsCardText = Color(c) }
    fun setActiveIconColor(c: Int)   { if (currentTab == 0) favRowIcon  = Color(c) else Unit }
}
