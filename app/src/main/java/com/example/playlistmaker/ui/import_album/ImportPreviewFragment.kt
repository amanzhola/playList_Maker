package com.example.playlistmaker.ui.import_album

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.os.BundleCompat
import androidx.core.os.bundleOf
import androidx.core.view.doOnLayout
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.playlistmaker.R
import com.example.playlistmaker.domain.api.base.NetworkStatusChecker
import com.example.playlistmaker.domain.models.search.Track
import com.example.playlistmaker.domain.repository.base.ResourceColorProvider
import com.example.playlistmaker.presentation.import_album.ImportPreviewViewModel
import com.example.playlistmaker.ui.audio.OnTrackClickListener
import com.example.playlistmaker.ui.audio.TrackAdapter
import com.example.playlistmaker.utils.ARG_IMPORT_URI
import com.example.playlistmaker.utils.ARG_PREFILL_COVER
import com.example.playlistmaker.utils.ARG_PREFILL_DESC
import com.example.playlistmaker.utils.ARG_PREFILL_NAME
import com.example.playlistmaker.utils.ARG_PREFILL_TRACKS
import com.example.playlistmaker.utils.EXTRA_IMPORT_ENTRY
import com.example.playlistmaker.utils.EXTRA_IMPORT_URI
import com.example.playlistmaker.utils.NavKeys
import com.example.playlistmaker.utils.makeSingleLineEllipsizeEnd
import com.example.playlistmaker.utils.setupDesc
import com.example.playlistmaker.utils.showLongSnack
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.core.parameter.parametersOf

class ImportPreviewFragment : Fragment(R.layout.fragment_import_preview), OnTrackClickListener {

    private var suppressToolbarOnPause: Boolean = false

    private val fromExternalImport by lazy {
        requireActivity().intent.getBooleanExtra(EXTRA_IMPORT_ENTRY, false)
    }


    // Если ты используешь Koin – замени на by viewModel(), остальное без изменений.
    private val vm: ImportPreviewViewModel by viewModels()
    private lateinit var behavior: BottomSheetBehavior<View>
    private lateinit var adapter: TrackAdapter

    // Вариант A (Safe Args)
//    private val args: ImportPreviewFragmentArgs by navArgs()
//    private val inputUri: Uri get() = args.argImportUri

//    // Вариант B (без Safe Args)
//    private val inputUri: Uri? by lazy {
//        // 1) из аргументов фрагмента
//        arguments?.let { BundleCompat.getParcelable(it, ARG_IMPORT_URI, Uri::class.java) }
//        // 2) из интента (deep link / share)
//            ?: requireActivity().intent?.data
//            ?: requireActivity().intent?.clipData?.getItemAt(0)?.uri
//    }

    private val inputUri: Uri? by lazy {
        // 1) из аргументов фрагмента
        arguments?.let { BundleCompat.getParcelable(it, ARG_IMPORT_URI, Uri::class.java) }
        // 2) ИЗ ИНТЕНТА: СНАЧАЛА EXTRA_IMPORT_URI (вот этого раньше не хватало)
            ?: run {
                val i = requireActivity().intent
                // начиная с API 33 лучше так:
                val fromExtra = androidx.core.os.BundleCompat.getParcelable(
                    i.extras ?: Bundle(), EXTRA_IMPORT_URI, Uri::class.java
                )
                fromExtra
                    ?: i.data
                    ?: i.clipData?.getItemAt(0)?.uri
            }
    }

    // для TrackAdapter инжект зависимости
    private val resourceColorProvider: ResourceColorProvider by inject { parametersOf(requireContext()) }
    private val networkChecker: NetworkStatusChecker by inject { parametersOf(requireContext()) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // header
        val ivCover = view.findViewById<ImageView>(R.id.ivCover)
        val tvTitle = view.findViewById<TextView>(R.id.tvTitle)
        val tvDesc  = view.findViewById<TextView>(R.id.tvDesc)
        val tvMeta  = view.findViewById<TextView>(R.id.tvMeta)
        val btnBack = view.findViewById<ImageButton>(R.id.btnBack)
        val btnSave = view.findViewById<ImageButton>(R.id.btnSave)

        // Название: ровно 1 строка + троеточие (для Info-экрана)
        tvTitle.makeSingleLineEllipsizeEnd()
//        tvDesc.makeSingleLineEllipsizeEnd()
        // или, если нужно N строк по ресурсу:
        val max = resources.getInteger(R.integer.qty_lines_create_playlist)

        btnBack.setOnClickListener {

            suppressToolbarOnPause = true
            (activity as? com.example.playlistmaker.BaseActivity)
                ?.toolbarHelper
                ?.hideToolbar()

            if (fromExternalImport) {
                requireActivity().finishAffinity()
            } else {
                val popped = findNavController().popBackStack()
                if (!popped) requireActivity().finish() // на случай если стек пуст
            }
        }

        // И системный «Назад» в этом фрагменте — тоже так же:
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (fromExternalImport) {
                        requireActivity().finishAffinity()
                    } else {
                        val popped = findNavController().popBackStack()
                        if (!popped) requireActivity().finish()
                    }
                }
            }
        )

        // bottom sheet с треками
        val sheet = view.findViewById<View>(R.id.tracks_sheet)
        behavior = BottomSheetBehavior.from(sheet).apply {
            isHideable = false
            isDraggable = true
            skipCollapsed = false
            isFitToContents = false
            expandedOffset = resources.getDimensionPixelSize(R.dimen.playlist_sheet_expanded_offset)
            state = BottomSheetBehavior.STATE_COLLAPSED
        }

        // ---- ДИНАМИЧЕСКИЙ пересчёт peekHeight от shareMenuRow ----
        val shareRow = view.findViewById<View>(R.id.shareMenuRow)
        val extraPx = resources.getDimensionPixelSize(R.dimen.peek_below_share_extra) // например 40dp

        // первый расчёт — когда shareRow уже отрисован
        shareRow.doOnLayout {
            updatePeekBelowShare(sheet, shareRow, extraPx, behavior)
        }

        val rv = view.findViewById<RecyclerView>(R.id.rvTracks)
        rv.layoutManager = LinearLayoutManager(requireContext())
        // можно использовать твой уже существующий TrackAdapter
        adapter = TrackAdapter(
            mutableListOf(),
            resourceColorProvider,
            networkChecker,
            this,
            R.id.action_global_to_extraOptionFragment
        )
        rv.adapter = adapter

        // загрузка
        inputUri?.let { uri ->
            // если пришло через SAF — захватим persisting read
            if ("content".equals(uri.scheme, ignoreCase = true)) {
                runCatching {
                    requireActivity().contentResolver.takePersistableUriPermission(
                        uri, Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                }
            }
            vm.load(requireContext(), uri)
        } ?: run {
            // нет Uri → покажем ошибку и уйдём назад
            Toast.makeText(requireContext(), "Файл не передан", Toast.LENGTH_SHORT).show()
            findNavController().navigateUp()
        }

        // observe
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                vm.state.collect { ui ->
                    // ошибки/лоадер по желанию
                    tvTitle.text = ui.name
                    tvDesc.isVisible = !ui.description.isNullOrBlank()
                    tvDesc.text = ui.description ?: "2022"

                    // выставляем режим под sw-ресурсы
                    setupDesc(tvDesc, max)

                    // если хочешь гарантировать запуск marquee после раскладки
                    if (max == 1) tvDesc.post { tvDesc.isSelected = true }

                    // после установки текста — на следующем кадре пересчёт
                    tvDesc.post {
                        updatePeekBelowShare(sheet, shareRow, extraPx, behavior)
                    }

                    val minutesText = resources.getQuantityString(
                        R.plurals.minutes_count, ui.minutesTotal.toInt(), ui.minutesTotal
                    )
                    val tracksText = resources.getQuantityString(
                        R.plurals.tracks_count, ui.tracksCount, ui.tracksCount
                    )
                    tvMeta.text = getString(R.string.two_parts_with_dot, minutesText, tracksText)

                    // обложка (если есть)
                    if (ui.coverUri != null) {
                        Glide.with(ivCover).load(ui.coverUri)
                            .placeholder(R.drawable.placeholder2)
                            .error(R.drawable.placeholder2)
                            .centerCrop()
                            .into(ivCover)
                    } else {
                        ivCover.setImageResource(R.drawable.placeholder2)
                    }

                    // список
                    adapter.updateTracks(ui.tracks.toMutableList())

                    // кнопка "Сохранить" → CreatePlaylistFragment с предзаполнением
                    btnSave.setOnClickListener {
                        val args = bundleOf(
                            ARG_PREFILL_NAME  to ui.name,
                            ARG_PREFILL_DESC  to ui.description,
                            ARG_PREFILL_COVER to ui.coverUri?.toString(),
                            ARG_PREFILL_TRACKS to ArrayList(ui.tracks)
                        )

                        // 👇 не даём onPause() показать тулбар во время перехода
                        suppressToolbarOnPause = true
                        (activity as? com.example.playlistmaker.BaseActivity)
                            ?.toolbarHelper
                            ?.hideToolbar()

                        findNavController().navigate(R.id.createPlaylistFragment, args)
                    }
                }
            }
        }

        // Слушаем результат из CreatePlaylistFragment
        findNavController().currentBackStackEntry?.savedStateHandle?.let { state ->
            state.getLiveData<String>(NavKeys.PLAYLIST_CREATED_NAME)
                .observe(viewLifecycleOwner) { name ->
                    // внутри используем уже НЕ nullable state
                    val count: Int? = state.get<Int>("PLAYLIST_CREATED_COUNT")

                    if (count != null && count > 0) {
                        val tracksText = resources.getQuantityString(R.plurals.tracks_count, count, count)
                        showLongSnack(getString(R.string.playlist_saved_with_count, name, tracksText))
                    } else {
                        showLongSnack(getString(R.string.playlist_saved, name))
                    }

                    // очищаем ключи, чтобы сообщение не повторялось
                    state.remove<String>(NavKeys.PLAYLIST_CREATED_NAME)
                    state.remove<Int>("PLAYLIST_CREATED_COUNT")
                }
        }
    }
    override fun onTrackClicked(track: Track) { /*  переход TrackAdapter (через action id), здесь можно лог/историю/аналитику*/ }
    override fun onArrowClicked(track: Track) { /*  TrackAdapter уже есть хук — но не используем */ }

    private fun updatePeekBelowShare(
        sheet: View,
        shareRow: View,
        extraPx: Int,
        behavior: BottomSheetBehavior<View>
    ) {
        val parent = sheet.parent as View
        parent.post {
            val parentLoc = IntArray(2)
            parent.getLocationOnScreen(parentLoc)
            val parentBottom = parentLoc[1] + parent.height

            val shareLoc = IntArray(2)
            shareRow.getLocationOnScreen(shareLoc)
            val shareBottom = shareLoc[1] + shareRow.height

            val desiredTop = shareBottom + extraPx
            val newPeek = (parentBottom - desiredTop).coerceIn(0, parent.height)

            if (behavior.peekHeight != newPeek) {
                behavior.peekHeight = newPeek
                if (behavior.state == BottomSheetBehavior.STATE_COLLAPSED) {
                    sheet.requestLayout()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        suppressToolbarOnPause = false
        (activity as? com.example.playlistmaker.BaseActivity)
            ?.toolbarHelper
            ?.hideToolbar()
    }

    override fun onPause() {
        if (!suppressToolbarOnPause) {
            (activity as? com.example.playlistmaker.BaseActivity)
                ?.toolbarHelper
                ?.showToolbar()
        }
        super.onPause()
    }

}
