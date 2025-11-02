package com.example.playlistmaker.ui.audio

import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.ViewGroup
import android.widget.TextView
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.playlistmaker.BaseActivity
import com.example.playlistmaker.BaseFragment
import com.example.playlistmaker.R
import com.example.playlistmaker.domain.api.base.NetworkStatusChecker
import com.example.playlistmaker.domain.repository.base.AudioTracksShare
import com.example.playlistmaker.presentation.searchViewModels.SearchViewModel
import com.example.playlistmaker.presentation.utils.BackgroundExclusionProvider
import com.example.playlistmaker.presentation.utils.ToolbarConfig
import com.example.playlistmaker.roots.main.MainActivity
import com.example.playlistmaker.ui.main.BottomNavConfig
import com.example.playlistmaker.ui.search.compose.SearchRoute
import com.example.playlistmaker.utils.showLongSnack
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class SearchFragment : BaseFragment(), BottomNavConfig, BackgroundExclusionProvider {

    private var searchScreenBg: Color? by mutableStateOf(null)

    private var listTextColorOverride: Color? by mutableStateOf(null)
    private var listIconColorOverride: Color? by mutableStateOf(null)
    private var listRowBgOverride: Color? by mutableStateOf(null)

    // --- VM и сервисы, которые реально нужны ---
    private val viewModel: SearchViewModel by viewModel()
    private val networkChecker: NetworkStatusChecker by inject { parametersOf(requireContext()) }
    private val trackShareService: AudioTracksShare by inject { parametersOf(requireContext()) }

    // --- нижняя навигация ---
    private var isBottomNavVisible = true
    private fun updateBottomNavVisibility(show: Boolean) {
        if (isBottomNavVisible != show) {
            if (show) getBaseActivity()?.showBottomNavigation()
            else getBaseActivity()?.hideBottomNavigation()
            isBottomNavVisible = show
        }
    }

    // --- сеть ---
    private var cm: ConnectivityManager? = null
    private var networkCallback: ConnectivityManager.NetworkCallback? = null
    private var lastConnected: Boolean? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1) ПОДПИСКА НА UI-СТЕЙТ → показать/скрыть нижнюю навигацию (как было)
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                // поток №1
                launch {
                    viewModel.uiState.collect { s ->
                        // показываем нижнюю навигацию, когда поле поиска пустое
                        updateBottomNavVisibility(s.query.isBlank())
                    }
                }

                // 2) ПОДПИСКА НА СОБЫТИЕ ОТКРЫТИЯ ТРЕКА → переход на плеер (как раньше через action + bundle)
                // поток №2
                launch {
                    viewModel.openTrack.collect { track ->
                        // список, который сейчас отрисован (история или результаты)
                        val list = viewModel.uiState.value.displayedTracks

                        // индекс кликнутого (по trackId), fallback 0
                        val index = list.indexOfFirst { it.trackId == track.trackId }
                            .let { if (it >= 0) it else 0 }

                        // сериализуем список в JSON (как делал адаптер раньше)
                        val json = com.google.gson.Gson().toJson(list)

                        // переходим тем же action и ключами, что у тебя в графе
                        findNavController().navigate(
                            R.id.action_searchFragment_to_extraOptionFragment,
                            androidx.core.os.bundleOf(
                                "TRACK_LIST_JSON" to json,
                                "TRACK_INDEX" to index
                            )
                        )
                    }
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val ctx = requireContext()

        // Корень: FrameLayout, чтобы можно было положить overlay поверх compose
        val root = android.widget.FrameLayout(ctx).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            id = View.generateViewId()
            // фон как в compose-экране
            setBackgroundColor(
                androidx.core.content.ContextCompat.getColor(ctx, R.color.white_textColor)
            )
        }

        // 1) Compose-контент
        val compose = ComposeView(ctx).apply {
            id = R.id.composeContent          // res/values/ids.xml
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setBackgroundColor(android.graphics.Color.TRANSPARENT)
            setContent {
                SearchRoute(
                    screenBackgroundOverride = searchScreenBg,
                    rowTextColorOverride    = listTextColorOverride,
                    rowIconColorOverride    = listIconColorOverride,
                    rowBackgroundOverride   = listRowBgOverride
                )
            }
        }

        // 2) fail.xml поверх. По дефолту в стилях у него GONE.
        val failView = layoutInflater.inflate(R.layout.fail, root, false) as TextView

        // Раскладка fail-а: для поиска “под инпутом”.
        // Возьмём подход: TOP + CENTER_HORIZONTAL и зазор сверху из дименов.
        val lp = android.widget.FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply {
            gravity = android.view.Gravity.TOP or android.view.Gravity.CENTER_HORIZONTAL
            // подвинем вниз на величину поля поиска + отступы
            topMargin = resources.getDimensionPixelSize(R.dimen.track_45)
        }
        failView.layoutParams = lp

        root.addView(compose)
        root.addView(failView)
        return root
    }

    // --- BottomNavConfig ---
    override fun getBottomNavButtonIndex(): Int = 0
    override fun shouldShowFullBottomNav(): Boolean = false
    override fun shouldShowBottomNav(): Boolean = true

    // --- Toolbar (оставляем как было) ---
    override fun getToolbarConfig(): ToolbarConfig =
        ToolbarConfig(GONE, R.string.search) {
            (requireActivity() as? MainActivity)?.apply {
                buttonIndex = -1
                switchFragment(buttonIndex)
                bottomNavigationHelper.selectButton(buttonIndex)
                bottomNavigationHelper.setBottomNavigationVisibility()
            }
        }

    fun setListTextColor(color: Int) {
        listTextColorOverride = Color(color)
    }

    fun setListArrowColor(color: Int) {
        listIconColorOverride = Color(color)
    }

    fun setListBackGroundColor(color: Int) {
        val c = Color(color)
        listRowBgOverride = c                // фон строк
        searchScreenBg   = c                 // и фон всего SearchScreen
    }

    fun shareTrackHistoryFromViewModel() {
        // достаём список из VM и шарим его тем же сервисом, что и раньше
        val tracks = viewModel.getTrackHistoryList()
        trackShareService.shareTracks(tracks, R.string.history_track)
    }

    @SuppressLint("ObsoleteSdkInt")
    override fun onResume() {
        super.onResume()

        requireActivity()
            .findViewById<TextView>(R.id.title)
            ?.visibility = View.VISIBLE

        val backStackEntry = findNavController().currentBackStackEntry
        val fromExtra = backStackEntry?.savedStateHandle?.get<Boolean>("from_extra") == true
        if (fromExtra) {
            backStackEntry?.savedStateHandle?.remove<Boolean>("from_extra")
            val query = viewModel.uiState.value.query
            if (query.isNotEmpty()) {
                getBaseActivity()?.hideBottomNavigation()
            } else {
                viewModel.setInputFocused(true)
            }
        }

        (activity as? BaseActivity)?.updateSegmentTexts()
        (activity as? BaseActivity)?.applyThemeThenRestoreSaved()

        cm = requireContext().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        lastConnected = networkChecker.isNetworkAvailable()

        networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) { lastConnected = true }
            override fun onLost(network: Network) {
                val now = networkChecker.isNetworkAvailable()
                val was = lastConnected
                if (was == true && !now) {
                    showLongSnack(
                        getString(R.string.no_internet_connection),
                        anchor = requireActivity().findViewById(R.id.bottomNavigation)
                    )
                }
                lastConnected = now
            }
            override fun onCapabilitiesChanged(network: Network, caps: NetworkCapabilities) {
                val now = networkChecker.isNetworkAvailable()
                val was = lastConnected
                if (was == true && !now) {
                    showLongSnack(
                        getString(R.string.no_internet_connection),
                        anchor = requireActivity().findViewById(R.id.bottomNavigation)
                    )
                }
                lastConnected = now
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            cm?.registerDefaultNetworkCallback(networkCallback!!)
        } else {
            val req = NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build()
            cm?.registerNetworkCallback(req, networkCallback!!)
        }
    }

    override fun onPause() {
        super.onPause()
        networkCallback?.let { cb -> runCatching { cm?.unregisterNetworkCallback(cb) } }
        networkCallback = null
        cm = null
    }

    // раньше было для исключения раскраски конкретных View id — их больше нет
    override fun backgroundExclusionIds(): Set<Int> = emptySet()
}
