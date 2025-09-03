package com.example.playlistmaker.ui.audioPosters

import android.content.Context
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
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
import com.example.playlistmaker.domain.models.search.Track
import com.example.playlistmaker.domain.repository.base.AudioSingleTrackShare
import com.example.playlistmaker.domain.repository.base.TrackListIntentParser
import com.example.playlistmaker.presentation.ImageLoader
import com.example.playlistmaker.presentation.searchPostersViewModels.ExtraOptionViewModel
import com.example.playlistmaker.presentation.utils.ToolbarConfig
import com.example.playlistmaker.roots.main.MainActivity
import com.example.playlistmaker.ui.main.BottomNavConfig
import com.example.playlistmaker.utils.NavKeys
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class ExtraOptionFragment : BaseFragment(), BottomNavConfig {

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
                // запускаем наблюдение за плеером, когда экран на виду
                // в VM есть защита от повторного старта
                viewModel.startObservingAudioPlayer()

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

        // ⭐ Подсветка иконки нижнего меню
        binding.root.findViewById<View>(R.id.bottom6)?.isSelected = true

        // ❤️ один раз включаем «живую» синхронизацию флагов из БД
        viewModel.startFavoritesSyncIfNeeded()
    }

    override fun onPause() {
        super.onPause()
        val pos = (binding.tracksRecyclerView.layoutManager as? LinearLayoutManager)
            ?.findFirstVisibleItemPosition() ?: 0
        viewModel.setScrollPosition(pos)
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
        ToolbarConfig(View.VISIBLE, R.string.option) {
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

    override fun onResume() {
        super.onResume()
        (activity as? BaseActivity)?.updateSegmentTexts()
        if (hideBottomSheetIfOpen()) {
            overlay.visibility = View.GONE
            overlay.alpha = 0f
        }

        // fixing theme on emulator and real mobile difference
        (activity as? BaseActivity)?.applyToolbarThemeColors()
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
