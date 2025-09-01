package com.example.playlistmaker.ui.playlistInfo

import android.content.Context
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
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
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf


class PlaylistInfoFragment : Fragment(R.layout.fragment_playlist_info), OnTrackClickListener {

    private val args: PlaylistInfoFragmentArgs by navArgs()
    private val viewModel: PlaylistInfoViewModel by viewModel()

    private fun lerp(a: Float, b: Float, t: Float) = a + (b - a) * t.coerceIn(0f, 1f)

    // ⚠️ адаптер пере-использован
    private lateinit var adapter: TrackAdapter
    private lateinit var behavior: BottomSheetBehavior<View>

    private lateinit var menuBehavior: BottomSheetBehavior<View>
    private lateinit var overlay: View
    private var overlayDefaultClickable = false
    private var overlayDefaultFocusable = false

    private lateinit var menuAdapter: MenuAdapter

    private fun coverModelFrom(path: String?): Any? =
        path?.takeIf { it.isNotBlank() }?.let { ref ->
            when {
                ref.startsWith("content://") || ref.startsWith("file://") -> ref.toUri()
                ref.startsWith("/") -> java.io.File(ref)
                ref.startsWith("http") -> ref
                else -> null
            }
        }
    private var lastUi: PlaylistInfoViewModel.Ui? = null

    // формат mm:ss
    private fun formatDuration(ms: Long): String {
        val totalSec = (ms / 1000).toInt()
        val m = totalSec / 60
        val s = totalSec % 60
        return "%d:%02d".format(m, s)
    }

    // единый сниackbar «нечем делиться»
    private fun showNothingToShareSnackbar(durationMs: Int = 4000) {
        val root = requireActivity().findViewById<View>(android.R.id.content)
        val msg = getString(R.string.nothing_to_share)
        val sb = com.google.android.material.snackbar.Snackbar
            .make(root, msg, com.google.android.material.snackbar.Snackbar.LENGTH_LONG)
        (sb.view.layoutParams as? ViewGroup.MarginLayoutParams)?.setMargins(0, 0, 0, 0)
        sb.duration = durationMs
        sb.show()
    }

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
        tvTitle.apply {
            isSingleLine = true
            maxLines = 1
            setHorizontallyScrolling(true)     // помогает, чтобы ellipsize работал предсказуемо
            ellipsize = TextUtils.TruncateAt.END
        }

        // Описание: ровно 1 строка + троеточие (для Info-экрана)
        tvDesc.apply {
            isSingleLine = true
            maxLines = 1
            setHorizontallyScrolling(true)
            ellipsize = TextUtils.TruncateAt.END
        }
/*
        // Опция -> Описание: ровно N(qty_lines_create_playlist) строк + троеточие (для Info-экрана)
        val maxDescLines = resources.getInteger(R.integer.qty_lines_create_playlist)
        tvDesc.apply {
            isSingleLine = false
            setHorizontallyScrolling(false)
            maxLines = maxDescLines
            ellipsize = TextUtils.TruncateAt.END
        }
*/
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
            showNothingToShareSnackbar()
            return
        }
        if (ui.tracks.isEmpty()) {
            showNothingToShareSnackbar()
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

            addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
                private fun applyOverlayForMenu(opened: Boolean) {
                    overlay.isClickable = if (opened) true else overlayDefaultClickable
                    overlay.isFocusable = if (opened) true else overlayDefaultFocusable

                    // плавно меняем альфу: к большему затемнению при открытом меню
                    overlay.animate()
                        .alpha(if (opened) MENU_DIM else BASE_DIM)
                        .setDuration(180L)
                        .start()
                }
                override fun onStateChanged(bottomSheet: View, newState: Int) {
                    val opened = newState != BottomSheetBehavior.STATE_HIDDEN
                    applyOverlayForMenu(opened)
                    if (!opened) { // вернуть дефолтные флаги
                        overlay.isClickable = overlayDefaultClickable
                        overlay.isFocusable = overlayDefaultFocusable
                    }
                }
                override fun onSlide(bottomSheet: View, slideOffset: Float) {
                    /* no-op */
                    // интерактивная анимация затемнения во время перетягивания
                    overlay.alpha = lerp(BASE_DIM, MENU_DIM, slideOffset)
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

    private fun AlertDialog.showWithSquareWhiteStyle(ctx: Context) {
        setOnShowListener {
            // фон прямоугольником
            window?.setBackgroundDrawable(
                ContextCompat.getDrawable(ctx, R.drawable.bg_dialog_square)
            )
            // кнопки
            val accent = ContextCompat.getColor(ctx, R.color.backgroundDay)
            getButton(AlertDialog.BUTTON_POSITIVE)?.setTextColor(accent)
            getButton(AlertDialog.BUTTON_NEGATIVE)?.setTextColor(accent)
            // тело сообщения
            findViewById<TextView>(android.R.id.message)
                ?.setTextColor(ContextCompat.getColor(ctx, R.color.textColor))
            // заголовок, если есть
            findTitleView()
                ?.setTextColor(ContextCompat.getColor(ctx, R.color.textColor))
        }
        show()
    }

    private fun AlertDialog.findTitleView(): TextView? {
        return findViewById(androidx.appcompat.R.id.alertTitle)
            ?: findViewById(com.google.android.material.R.id.alertTitle)
            ?: findViewById(android.R.id.title)
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
