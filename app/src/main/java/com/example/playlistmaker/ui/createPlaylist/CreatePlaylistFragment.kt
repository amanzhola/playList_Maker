package com.example.playlistmaker.ui.createPlaylist

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.compose.BackHandler
import androidx.annotation.StringRes
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.ComposeView
import androidx.core.net.toUri
import androidx.core.os.BundleCompat
import androidx.core.os.bundleOf
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.navOptions
import com.example.playlistmaker.BaseActivity
import com.example.playlistmaker.BaseFragment
import com.example.playlistmaker.R
import com.example.playlistmaker.domain.models.search.Track
import com.example.playlistmaker.presentation.createPlaylist.CreatePlaylistViewModel
import com.example.playlistmaker.presentation.utils.ToolbarConfig
import com.example.playlistmaker.ui.createPlaylist.compose.CreatePlaylistScreen
import com.example.playlistmaker.ui.main.BottomNavConfig
import com.example.playlistmaker.utils.ARG_EDIT_COVER
import com.example.playlistmaker.utils.ARG_EDIT_DESC
import com.example.playlistmaker.utils.ARG_EDIT_ID
import com.example.playlistmaker.utils.ARG_EDIT_NAME
import com.example.playlistmaker.utils.ARG_PREFILL_COVER
import com.example.playlistmaker.utils.ARG_PREFILL_DESC
import com.example.playlistmaker.utils.ARG_PREFILL_NAME
import com.example.playlistmaker.utils.ARG_PREFILL_TRACKS
import com.example.playlistmaker.utils.NavKeys
import com.example.playlistmaker.utils.NavKeys.SCROLL_TOP
import com.example.playlistmaker.utils.showWithSquareWhiteStyle
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.File

class CreatePlaylistFragment : BaseFragment(), BottomNavConfig {

    private val vm: CreatePlaylistViewModel by viewModel()

    private var importTracksCount: Int? = null
    private val isEditMode get() = arguments?.containsKey(ARG_EDIT_ID) == true
    private var exitDialogShown = false

    // ⚙️ Используем ComposeView вместо XML-лейаута. ViewBinding больше не нужен.
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View = ComposeView(requireContext()).apply {
        id = View.generateViewId()
        layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
        )
    }

    @SuppressLint("UseKtx")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        (activity as? BaseActivity)?.enableEdgeToEdge(false)

        // ⚙️ Инициализация VM по аргументам — 1:1 с кодом
        if (isEditMode) {
            val id    = requireArguments().getLong(ARG_EDIT_ID)
            val name  = requireArguments().getString(ARG_EDIT_NAME).orEmpty()
            val desc  = requireArguments().getString(ARG_EDIT_DESC)
            val cover: Uri? = requireArguments().getString(ARG_EDIT_COVER)
                ?.let { s ->
                    // 1) content:// -> Uri.parse
                    runCatching { Uri.parse(s) }.getOrNull()
                    // 2) /data/... -> File -> toUri()
                        ?: File(s).takeIf { it.exists() }?.toUri()
                }
            vm.enterEditModeIfNeeded(id, name, desc, cover)
        } else {
            val preName  = arguments?.getString(ARG_PREFILL_NAME)
            val preDesc  = arguments?.getString(ARG_PREFILL_DESC)
            val preCover = arguments?.getString(ARG_PREFILL_COVER)
            val preTracks: List<Track> =
                arguments?.let { BundleCompat.getParcelableArrayList(it, ARG_PREFILL_TRACKS, Track::class.java) }
                    ?: emptyList()

            importTracksCount = preTracks.size.takeIf { it > 0 }
            if (!preName.isNullOrBlank() || !preDesc.isNullOrBlank() || !preCover.isNullOrBlank() || preTracks.isNotEmpty()) {
                vm.prefillForCreate(preName.orEmpty(), preDesc, preCover, preTracks)
            }
        }

        // ⚙️ Рендерим Compose-экран и подписываемся на стейт/ивенты
        (view as ComposeView).setContent {
            // ✅ Безопасная подписка на StateFlow с учётом lifecycle
            val state = vm.state.collectAsStateWithLifecycle().value

            // ✅ Системный back/жест назад — теперь через BackHandler в Compose
            BackHandler { handleBack() }

            // ✅ Одноразовые события VM → навигация/тосты остаются в фрагменте
            LaunchedEffect(vm) {
                vm.events.collect { e ->
                    when (e) {
                        is CreatePlaylistViewModel.Event.Saved -> onSaved(e.name)
                        is CreatePlaylistViewModel.Event.Error -> {
                            // TODO: показать Snackbar/Toast если нужно
                        }
                    }
                }
            }

            // ✅ Сам экран: чистый UI, все действия пробрасываем в VM/фрагмент
            CreatePlaylistScreen(
                state = state,
                isEditMode = isEditMode,
                onNameChanged = vm::onNameChanged,
                onDescChanged = vm::onDescChanged,
                onPickCover  = vm::onCoverPicked,
                // ⚙️ Лучше applicationContext: внутри save идёт IO/копирование в файловую систему
                onSave       = { vm.save(requireContext().applicationContext) },
                onBack       = { handleBack() }
            )
        }
    }

    // ✅ Навигация после сохранения — перенесена из старого кода без изменений
    private fun onSaved(name: String) {
        val nav = findNavController()
        val fromPlaylist = arguments?.getBoolean("from_playlist") == true
        val fromPreview  = arguments?.getBoolean("from_preview")  == true

        if (isEditMode) {
            nav.popBackStack(); return
        }

        when {
            fromPlaylist -> {
                val args = bundleOf(
                    NavKeys.PLAYLIST_CREATED_NAME to name,
                    SCROLL_TOP to true,
                    NavKeys.SELECT_TAB to 1
                )
                val opts = navOptions {
                    popUpTo(R.id.mediaLibraryFragment) {
                        inclusive = true
                        saveState = false
                    }
                    launchSingleTop = true
                    restoreState = false
                }
                nav.navigate(R.id.mediaLibraryFragment, args, opts)
            }
            fromPreview -> {
                requireActivity().setResult(
                    Activity.RESULT_OK, Intent().putExtra("playlist_created_name", name)
                )
                requireActivity().finish()
            }
            else -> {
                val prev = nav.previousBackStackEntry?.savedStateHandle
                prev?.set(NavKeys.PLAYLIST_CREATED_NAME, name)
                importTracksCount?.let { prev?.set("PLAYLIST_CREATED_COUNT", it) }
                nav.popBackStack()
            }
        }
    }

    // ✅ Вся логика back/диалога — оставляем в фрагменте (родитель продолжает управлять тулбаром)
    private fun handleBack() {
        if (!isAdded) return

        val fromPlaylist = arguments?.getBoolean("from_playlist") == true
        val fromPreview  = arguments?.getBoolean("from_preview")  == true

        val doExit = {
            when {
                isEditMode -> safePopBack()
                fromPlaylist -> {
                    runCatching {
                        val mediaEntry = findNavController().getBackStackEntry(R.id.mediaLibraryFragment)
                        mediaEntry.savedStateHandle.set(NavKeys.SELECT_TAB, 1)
                    }
                    safePopBack()
                }
                fromPreview -> {
                    requireActivity().setResult(Activity.RESULT_CANCELED)
                    requireActivity().finish()
                }
                else -> safePopBack()
            }
        }

        val hasChanges = vm.hasUnsavedChanges()
        if (isEditMode) {
            if (hasChanges) showExitDialog(titleRes = R.string.exit_dialog_edit, onConfirm = doExit)
            else doExit()
        } else {
            if (hasChanges) showExitDialog(onConfirm = doExit)
            else doExit()
        }
    }

    private fun safePopBack() {
        if (!isAdded) return
        val nav = runCatching { findNavController() }.getOrNull()
        val handled = when {
            nav == null -> false
            nav.navigateUp() -> true
            nav.popBackStack() -> true
            else -> false
        }
        if (!handled) requireActivity().finish()
    }

    private fun showExitDialog(
        @StringRes titleRes: Int = R.string.exit_dialog_title,
        @StringRes messageRes: Int = R.string.exit_dialog_message,
        onConfirm: () -> Unit = { safePopBack() },
        onCancel: (() -> Unit)? = null
    ) {
        if (exitDialogShown) return
        exitDialogShown = true
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(titleRes))
            .setMessage(getString(messageRes))
            .setNegativeButton(R.string.cancel) { d, _ -> d.dismiss(); onCancel?.invoke() }
            .setPositiveButton(R.string.finish) { d, _ -> d.dismiss(); onConfirm() }
            .setBackgroundInsetStart(0)
            .setBackgroundInsetEnd(0)
            .setBackgroundInsetTop(0)
            .setBackgroundInsetBottom(0)
            .create()
            .apply { setOnDismissListener { exitDialogShown = false } }
            .showWithSquareWhiteStyle(requireContext())
    }

    // ✅ Родитель продолжает управлять тулбаром/боттом-навом
    override fun getBottomNavButtonIndex(): Int? = null
    override fun shouldShowFullBottomNav(): Boolean = false
    override fun shouldShowBottomNav(): Boolean = false

    override fun getToolbarConfig(): ToolbarConfig =
        ToolbarConfig(View.VISIBLE, resolveTitleRes()) { handleBack() }

    private fun resolveTitleRes(): Int =
        if (isEditMode) R.string.edit_playlist_title
        else if ((importTracksCount ?: 0) > 0) R.string.import_preview_title
        else R.string.create_playlist_title

    // Оставляем, если есть различия темы между девайсами/эмулятором
    @Suppress("DEPRECATION")
    override fun onResume() {
        super.onResume()
        // Включаем resize только на время этого экрана
        requireActivity().window.setSoftInputMode(
            android.view.WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
        )
        (activity as? BaseActivity)?.applyThemeThenRestoreSaved()
    }

    override fun onPause() {
        super.onPause()
        // Возвращаем стандартный режим, чтобы не поломать другие экраны
        requireActivity().window.setSoftInputMode(
            android.view.WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING
        )
    }
}
