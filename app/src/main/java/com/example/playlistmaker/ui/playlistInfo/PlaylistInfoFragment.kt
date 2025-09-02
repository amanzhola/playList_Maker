package com.example.playlistmaker.ui.playlistInfo

import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.playlistmaker.R
import com.example.playlistmaker.domain.api.base.NetworkStatusChecker
import com.example.playlistmaker.domain.models.search.Track
import com.example.playlistmaker.domain.repository.base.ResourceColorProvider
import com.example.playlistmaker.presentation.playlistInfo.PlaylistInfoViewModel
import com.example.playlistmaker.ui.audio.OnTrackClickListener
import com.example.playlistmaker.ui.audio.TrackAdapter
import com.example.playlistmaker.ui.playlistInfo.model.MenuRow
import com.example.playlistmaker.utils.ACTION_DELETE
import com.example.playlistmaker.utils.ACTION_EDIT
import com.example.playlistmaker.utils.ACTION_SHARE
import com.example.playlistmaker.utils.ARG_EDIT_COVER
import com.example.playlistmaker.utils.ARG_EDIT_DESC
import com.example.playlistmaker.utils.ARG_EDIT_ID
import com.example.playlistmaker.utils.ARG_EDIT_NAME
import com.example.playlistmaker.utils.BASE_DIM
import com.example.playlistmaker.utils.MENU_DIM
import com.example.playlistmaker.utils.coverModelFrom
import com.example.playlistmaker.utils.formatDuration
import com.example.playlistmaker.utils.makeSingleLineEllipsizeEnd
import com.example.playlistmaker.utils.setStartDrawable
import com.example.playlistmaker.utils.setTopPaddingDp
import com.example.playlistmaker.utils.showLongSnack
import com.example.playlistmaker.utils.showWithSquareWhiteStyle
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class PlaylistInfoFragment : Fragment(R.layout.fragment_playlist_info), OnTrackClickListener {

    private var isMenuOpened = false
    private val args: PlaylistInfoFragmentArgs by navArgs()
    private val viewModel: PlaylistInfoViewModel by viewModel()

    // ⚠️ адаптер пере-использован
    private lateinit var adapter: TrackAdapter
    private lateinit var behavior: BottomSheetBehavior<View>

    private lateinit var menuBehavior: BottomSheetBehavior<View>
    private lateinit var overlay: View
    private var overlayDefaultClickable = false
    private var overlayDefaultFocusable = false

    private lateinit var menuAdapter: MenuAdapter
    private var lastUi: PlaylistInfoViewModel.Ui? = null

    // Текст по ТЗ
    private fun buildShareText(ui: PlaylistInfoViewModel.Ui): String {
        val sb = StringBuilder()
        sb.appendLine(ui.name)                                  // название
        if (!ui.description.isNullOrBlank()) sb.appendLine(ui.description) // описание (если есть)
        sb.appendLine(                                          // "[xx] треков"
            resources.getQuantityString(R.plurals.tracks_count, ui.tracksCount, ui.tracksCount)
        )

        // "N. Исполнитель - Название (mm:ss)"
        ui.tracks.forEachIndexed { i, t ->
            sb.append(i + 1).append(". ")
                .append(t.artistName).append(" - ").append(t.trackName)
                .append(" (").append(formatDuration(t.trackTimeMillis)).appendLine(")")
        }
        return sb.toString().trimEnd()
    }

    // для TrackAdapter инжект зависимости
    private val resourceColorProvider: ResourceColorProvider by inject { parametersOf(requireContext()) }
    private val networkChecker: NetworkStatusChecker by inject { parametersOf(requireContext()) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        val failTextView: TextView = view.findViewById(R.id.fail)
        failTextView.text = getString(R.string.no_tracks_in_playlist)
        failTextView.setStartDrawable(
            drawableRes = R.drawable.fail_icon,
            paddingPx = resources.getDimensionPixelSize(R.dimen.Padding_16)
        )
        val topPx = resources.getDimensionPixelSize(R.dimen.radius_2dp)
        failTextView.setTopPaddingDp(topPx)

        // ИНИЦИАЛИЗИРУЕМ overlay РАНЬШЕ, чтобы им пользоваться в collect{}
        overlay = view.findViewById(R.id.overlay)
        overlayDefaultClickable = overlay.isClickable
        overlayDefaultFocusable = overlay.isFocusable

        val ivCover = view.findViewById<ImageView>(R.id.ivCover)
        val tvTitle = view.findViewById<TextView>(R.id.tvTitle)
        val tvDesc  = view.findViewById<TextView>(R.id.tvDesc)
        val tvMeta  = view.findViewById<TextView>(R.id.tvMeta)
        val btnBack = view.findViewById<ImageButton>(R.id.btnBack)

        // Название: ровно 1 строка + троеточие (для Info-экрана)
        tvTitle.makeSingleLineEllipsizeEnd()
        tvDesc.makeSingleLineEllipsizeEnd()
        // или, если нужно N строк по ресурсу:
        // tvDesc.makeEllipsizeEnd(resources.getInteger(R.integer.qty_lines_create_playlist))

        btnBack.setOnClickListener { findNavController().navigateUp() }

        // --- BottomSheet: важно сохранить в поле behavior
        val sheet = view.findViewById<View>(R.id.tracks_sheet)
        behavior = BottomSheetBehavior.from(sheet).apply {
            isHideable = false
            isDraggable = true
            skipCollapsed = false
            isFitToContents = true
            peekHeight = resources.getDimensionPixelSize(R.dimen.playlist_sheet_peek)
            expandedOffset = resources.getDimensionPixelSize(R.dimen.playlist_sheet_expanded_offset)
            state = BottomSheetBehavior.STATE_COLLAPSED
        }

        // --- RecyclerView на адаптером
        val rv = view.findViewById<RecyclerView>(R.id.rvTracks)
        rv.layoutManager = LinearLayoutManager(requireContext())
        adapter = TrackAdapter(
            mutableListOf(),
            resourceColorProvider,
            networkChecker,
            this, // OnTrackClickListener
            R.id.action_global_to_extraOptionFragment // навигация
        )
        rv.adapter = adapter

        //используем -> долгий тап → удалить
        adapter.onItemLongClick = {track -> confirmDelete(track)}

        // --- Подписка на состояние
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState(args.playlistId).collect { ui ->

                    lastUi = ui

                    tvTitle.text = ui.name
                    tvDesc.isVisible = !ui.description.isNullOrBlank()
                    tvDesc.text = ui.description ?: "2022"

                    val minutesText = resources.getQuantityString(
                        R.plurals.minutes_count, ui.minutesTotal.toInt(), ui.minutesTotal
                    )
                    val tracksText = resources.getQuantityString(
                        R.plurals.tracks_count, ui.tracksCount, ui.tracksCount
                    )
                    tvMeta.text = getString(R.string.two_parts_with_dot, minutesText, tracksText)

                    // обложка
                    val model = coverModelFrom(ui.coverPath)

                    if (model != null) {
                        Glide.with(ivCover).load(model)
                            .placeholder(R.drawable.placeholder2)
                            .error(R.drawable.placeholder2)
                            .centerCrop()
                            .into(ivCover)
                    } else ivCover.setImageResource(R.drawable.placeholder2)

                    // ⚠️ загрузка адаптера
                    adapter.updateTracks(ui.tracks.toMutableList())
                    val showEmpty = ui.tracks.toMutableList().isEmpty()
                    failTextView.isVisible = showEmpty
                    failTextView.isEnabled = showEmpty

                }
            }
        }

        // кнопка «Поделиться» на экране
        view.findViewById<ImageButton>(R.id.btnShare).setOnClickListener {shareCurrentPlaylistOrToast()}

        // открывать меню ТОЛЬКО по кнопке // Назначаем клик на «Меню» безопасно
        view.findViewById<ImageButton>(R.id.btnMenu).setOnClickListener {
            ensureMenuInit()   // если ещё не инициализировано — инициализируем
            openMenuSheet()    // показываем меню
        }
    }

    override fun onTrackClicked(track: Track) { /*  переход TrackAdapter (через action id), здесь можно лог/историю/аналитику*/ }
    override fun onArrowClicked(track: Track) { /*  TrackAdapter уже есть хук — но не используем */ }

    private fun shareCurrentPlaylistOrToast() {
        val ui = lastUi ?: run {
            // единый сниackbar «нечем делиться»
            showLongSnack(getString(R.string.nothing_to_share))
            return
        }
        if (ui.tracks.isEmpty()) {
            // единый сниackbar «нечем делиться»
            showLongSnack(getString(R.string.nothing_to_share))
            return
        }

        // Формируем текст по ТЗ (название, описание, "[xx] треков", нумерованный список)
        val text = buildShareText(ui)

        // стандартный ACTION_SEND
        val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(android.content.Intent.EXTRA_TEXT, text)
        }
        startActivity(android.content.Intent.createChooser(intent, getString(R.string.share)))
    }

    /** Ленивая инициализация второго bottom sheet (меню). Вызывай перед любым доступом к нему. */
    private fun ensureMenuInit(root: View = requireView()) {

        overlay.alpha = BASE_DIM // ⬅️ базовое (лёгкое) затемнение

        if (::menuBehavior.isInitialized && ::menuAdapter.isInitialized) return

        val menuSheet = root.findViewById<View>(R.id.menu_sheet)
            ?: error("layout must contain @id/menu_sheet")

        // tap по затемнению закрывает только МЕНЮ (первый шит при этом не трогаем)
        overlay.setOnClickListener {
            if (::menuBehavior.isInitialized &&
                menuBehavior.state != BottomSheetBehavior.STATE_HIDDEN
            ) {
                menuBehavior.state = BottomSheetBehavior.STATE_HIDDEN
            }
        }

        // RV меню
        val rvMenu = root.findViewById<RecyclerView>(R.id.rvMenu)
            ?: error("layout must contain @id/rvMenu")
        rvMenu.layoutManager = LinearLayoutManager(requireContext())
        if (!::menuAdapter.isInitialized) {
            menuAdapter = MenuAdapter { actionId ->
                when (actionId) {
                    ACTION_SHARE  -> { shareCurrentPlaylistOrToast(); menuBehavior.state = BottomSheetBehavior.STATE_HIDDEN }
                    ACTION_EDIT   -> {
                    /* по ТЗ шаг 5 */
                        val ui = lastUi
                        if (ui == null) {
                            // нет актуального UI — просто закрываем меню и выходим из ветки
                            menuBehavior.state = BottomSheetBehavior.STATE_HIDDEN
                        } else {
                            findNavController().navigate(
                                R.id.createPlaylistFragment,
                                bundleOf(
                                    ARG_EDIT_ID    to args.playlistId, // <-- id из SafeArgs
                                    ARG_EDIT_NAME  to ui.name,
                                    ARG_EDIT_DESC  to ui.description,
                                    ARG_EDIT_COVER to ui.coverPath     // <-- строка пути/URI
                                )
                            )
                            // опционально сразу прячем меню
                            menuBehavior.state = BottomSheetBehavior.STATE_HIDDEN
                        }
                    }
                    ACTION_DELETE -> {
                        menuBehavior.state = BottomSheetBehavior.STATE_HIDDEN
                        confirmDeletePlaylist()
                    }
                }
            }
        }
        rvMenu.adapter = menuAdapter

        // Поведение меню
        menuBehavior = BottomSheetBehavior.from(menuSheet).apply {
            isHideable = true
            isDraggable = true
            state = BottomSheetBehavior.STATE_HIDDEN
            peekHeight = resources.getDimensionPixelSize(R.dimen.playlist_sheet_peek2)
            expandedOffset = resources.getDimensionPixelSize(R.dimen.playlist_sheet_expanded_offset)

            // 🔐 гарантируем чёрный фон (важно!)
            overlay.setBackgroundColor(0xFF000000.toInt())

            addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {

                private fun applyOverlayForMenu(opened: Boolean) {
                    overlay.isClickable = if (opened) true else overlayDefaultClickable
                    overlay.isFocusable = if (opened) true else overlayDefaultFocusable
                    // ⛔️ НИКАКИХ .animate().alpha() здесь!
                }

                override fun onStateChanged(bottomSheet: View, newState: Int) {
                    val opened = newState != BottomSheetBehavior.STATE_HIDDEN
                    isMenuOpened = opened
                    applyOverlayForMenu(opened)

                    overlay.animate().cancel()

                    when (newState) {
                        BottomSheetBehavior.STATE_EXPANDED -> {
                            overlay.alpha = 1f       // максимум
                        }
                        BottomSheetBehavior.STATE_COLLAPSED -> {
                            overlay.alpha = MENU_DIM // старт при показе меню
                        }
                        BottomSheetBehavior.STATE_HIDDEN -> {
                            overlay.alpha = BASE_DIM // базовое затемнение
                        }
                        else -> Unit
                    }

                    if (!opened) {
                        overlay.isClickable = overlayDefaultClickable
                        overlay.isFocusable = overlayDefaultFocusable
                    }
                }

                // убиица времени -> эмулятор(наконец-то удалось!)
                override fun onSlide(bottomSheet: View, slideOffset: Float) {
                    val raw = if (slideOffset.isNaN()) 0f else slideOffset
                    val t = raw.coerceIn(0f, 1f) // 0..1

                    overlay.animate().cancel()

                    if (isMenuOpened) {
                        // Меню уже показано (COLLAPSED→EXPANDED)
                        // целевая альфа: от MENU_DIM до 1f
                        val target = MENU_DIM + (1f - MENU_DIM) * t

                        // ⚙️ ВВЕРХ — только темнее (монотонность),
                        // если вдруг target < текущее (например, после глитча) — не светлим
                        if (target >= overlay.alpha) {
                            overlay.alpha = target
                        } else {
                            // ДВИЖЕНИЕ ВНИЗ → сразу к BASE_DIM
                            overlay.alpha = BASE_DIM
                        }
                    } else {
                        // Переход из HIDDEN к COLLAPSED (если перетягивание жестом)
                        // от BASE_DIM до MENU_DIM
                        val target = BASE_DIM + (MENU_DIM - BASE_DIM) * t
                        overlay.alpha = target
                    }
                }
            })
        }

    }

    /** Открыть меню: заполняем и раскрываем. Безопасно, т.к. ensureMenuInit() уже был вызван. */
    private fun openMenuSheet() {

        val ui = lastUi ?: return
        val header = MenuRow.Header(
            cover = coverModelFrom(ui.coverPath),
            name  = ui.name,
            count = resources.getQuantityString(R.plurals.tracks_count, ui.tracksCount, ui.tracksCount)
        )
        val items = listOf(
            MenuRow.Action(ACTION_SHARE,  getString(R.string.share)),
            MenuRow.Action(ACTION_EDIT,   getString(R.string.edit)),
            MenuRow.Action(ACTION_DELETE, getString(R.string.delete_playlist)),
        )
        menuAdapter.submit(listOf(header) + items)
        // ⬇️ вместо EXPANDED
        menuBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
    }

    private fun confirmDelete(track: Track) {
        MaterialAlertDialogBuilder(requireContext())
            .setMessage(R.string.delete_track_question)
            .setNegativeButton(R.string.no, null)
            .setPositiveButton(R.string.yes) { _, _ ->
                viewLifecycleOwner.lifecycleScope.launch {
                    viewModel.removeTrack(args.playlistId, track.trackId)
                }
            }
            .create()
            .showWithSquareWhiteStyle(requireContext())
    }

    private fun confirmDeletePlaylist() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.delete_playlist)
            .setMessage(R.string.delete_playlist_question)
            .setNegativeButton(R.string.no, null)
            .setPositiveButton(R.string.yes) { _, _ ->
                viewLifecycleOwner.lifecycleScope.launch {
                    viewModel.deletePlaylist(args.playlistId)
                    findNavController().navigateUp()
                }
            }
            .create()
            .showWithSquareWhiteStyle(requireContext())
    }
}
