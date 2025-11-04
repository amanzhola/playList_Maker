@file:Suppress("FunctionName")

package com.example.playlistmaker.ui.playlistInfo

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.compose.BackHandler
import androidx.compose.ui.platform.ComposeView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.playlistmaker.BaseActivity
import com.example.playlistmaker.R
import com.example.playlistmaker.domain.api.base.NetworkStatusChecker
import com.example.playlistmaker.domain.models.search.Track
import com.example.playlistmaker.presentation.playlistInfo.PlaylistInfoViewModel
import com.example.playlistmaker.ui.playlistInfo.compose.PlaylistInfoScreen
import com.example.playlistmaker.utils.ARG_EDIT_COVER
import com.example.playlistmaker.utils.ARG_EDIT_DESC
import com.example.playlistmaker.utils.ARG_EDIT_ID
import com.example.playlistmaker.utils.ARG_EDIT_NAME
import com.example.playlistmaker.utils.NavKeys
import com.example.playlistmaker.utils.PlaylistExport
import com.example.playlistmaker.utils.formatDuration
import com.example.playlistmaker.utils.showFailOrSnack
import com.example.playlistmaker.utils.showLongSnack
import com.example.playlistmaker.utils.showWithSquareWhiteStyle
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch
import org.koin.android.ext.android.getKoin
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class PlaylistInfoFragment : Fragment() {

    private var suppressToolbarOnPause: Boolean = false
    private val args: PlaylistInfoFragmentArgs by navArgs()
    private val vm: PlaylistInfoViewModel by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View = ComposeView(requireContext()).apply {
        id = View.generateViewId()
        layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        (view as ComposeView).setContent {
            val ui = vm.uiState(args.playlistId).collectAsStateWithLifecycle(initialValue = null).value

            // системный back — как было
            BackHandler {
                returnToMediaPlaylistsTab()
                findNavController().navigateUp()
            }

            ui?.let { state ->
                PlaylistInfoScreen(
                    ui = state,
                    onBack = {
                        returnToMediaPlaylistsTab()
                        findNavController().navigateUp()
                    },
                    onShare = { shareCurrentPlaylistOrSnack(state) },
                    onEdit = {
                        val b = bundleOf(
                            ARG_EDIT_ID    to args.playlistId,
                            ARG_EDIT_NAME  to state.name,
                            ARG_EDIT_DESC  to state.description,
                            ARG_EDIT_COVER to state.coverPath
                        )
                        findNavController().navigate(R.id.createPlaylistFragment, b)
                    },
                    onDeletePlaylist = {
                        confirmDeletePlaylist(
                            onConfirm = {
                                viewLifecycleOwner.lifecycleScope.launch {
                                    vm.deletePlaylist(args.playlistId)
                                    returnToMediaPlaylistsTab()
                                    findNavController().navigateUp()
                                }
                            }
                        )
                    },
                    onTrackClick = { tracks, index ->
                        // сериализуем список в JSON и передаём вместе с индексом
                        val gson = com.google.gson.Gson()
                        val trackListJson = gson.toJson(tracks)

                        val bundle = bundleOf(
                            "TRACK_LIST_JSON" to trackListJson,
                            "TRACK_INDEX"     to index,
                            "IS_FROM_SEARCH"  to false
                        )

                        findNavController().navigate(
                            R.id.action_global_to_extraOptionFragment,
                            bundle
                        )
                    },
                    onTrackLongClick = { track ->
                        confirmDeleteTrack(track) {
                            viewLifecycleOwner.lifecycleScope.launch {
                                vm.removeTrack(args.playlistId, track.trackId)
                            }
                        }
                    }
                )
            }
        }
    }

    /* --------- вспомогательные: диалоги / share / возврат --------- */

    @SuppressLint("QueryPermissionsNeeded")
    private fun shareCurrentPlaylistOrSnack(ui: PlaylistInfoViewModel.Ui) {
        if (ui.tracks.isEmpty()) {
            requireActivity().showLongSnack(getString(R.string.nothing_to_share)); return
        }
        val checker: NetworkStatusChecker =
            getKoin().get { parametersOf(requireContext().applicationContext) }
        if (!checker.isNetworkAvailable()) {
            requireActivity().showFailOrSnack(isSupport = false); return
        }

        viewLifecycleOwner.lifecycleScope.launch {
            val uri = PlaylistExport.exportToZip(requireContext(), ui) ?: run {
                requireActivity().showLongSnack(getString(R.string.export_failed)); return@launch
            }

            val share = Intent(Intent.ACTION_SEND).apply {
                type = "application/zip"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_TEXT, buildShareText(ui))
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                clipData = android.content.ClipData.newRawUri("playlist", uri)
            }

            // Раздать read-grant всем потенциальным получателям (иногда без этого падает «Невозможно открыть»)
            val pm = requireContext().packageManager
            val res = pm.queryIntentActivities(share, 0)
            for (ri in res) {
                requireContext().grantUriPermission(
                    ri.activityInfo.packageName,
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            }

            // 👇 предотвратить показ тулбара, пока открыт chooser
            suppressToolbarOnPause = true
            (activity as? BaseActivity)?.toolbarHelper?.hideToolbar()

            startActivity(Intent.createChooser(share, getString(R.string.share)))
        }
    }

    private fun confirmDeleteTrack(track: Track, onYes: () -> Unit) {
        MaterialAlertDialogBuilder(requireContext())
            .setMessage(R.string.delete_track_question)
            .setNegativeButton(R.string.no, null)
            .setPositiveButton(R.string.yes) { _, _ -> onYes() }
            .create()
            .showWithSquareWhiteStyle(requireContext())
    }

    @SuppressLint("UseKtx")
    private fun confirmDeletePlaylist(onConfirm: () -> Unit) {
        val dlg = com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.delete_playlist)
            .setMessage(R.string.delete_playlist_question)
            .setNegativeButton(R.string.no, null)
            .setPositiveButton(R.string.yes) { _, _ -> onConfirm() }
            // 👇 белая «карточка» самого диалога
            .setBackground(
                androidx.core.content.ContextCompat.getDrawable(
                    requireContext(), R.drawable.bg_dialog_square
                )
            )
            // 👇 убираем чёрные поля-инсеты вокруг карточки
            .setBackgroundInsetStart(0)
            .setBackgroundInsetEnd(0)
            .setBackgroundInsetTop(0)
            .setBackgroundInsetBottom(0)
            .create()
            .apply {
                setOnShowListener {
                    // 👇 убираем затемнение вокруг и фон окна (иначе просвечивают углы)
                    window?.setDimAmount(0f)
                    window?.setBackgroundDrawable(androidx.core.content.ContextCompat.getDrawable(
                        requireContext(), R.color.white_textColor))
                }
            }

        // единый хелпер можно не трогать; либо сразу show()
        dlg.show()
    }

    private fun returnToMediaPlaylistsTab() {
        val mediaEntry = findNavController().getBackStackEntry(R.id.mediaLibraryFragment)
        mediaEntry.savedStateHandle.set(NavKeys.SELECT_TAB, 1)
        mediaEntry.savedStateHandle.set(NavKeys.RESTORE_PLAYLIST_ID, args.playlistId)
    }

    private fun buildShareText(ui: PlaylistInfoViewModel.Ui): String {
        val sb = StringBuilder()
        sb.appendLine(ui.name)
        if (!ui.description.isNullOrBlank()) sb.appendLine(ui.description)
        sb.appendLine(
            resources.getQuantityString(R.plurals.tracks_count, ui.tracksCount, ui.tracksCount)
        )
        ui.tracks.forEachIndexed { i, t ->
            sb.append(i + 1).append(". ")
                .append(t.artistName).append(" - ").append(t.trackName)
                .append(" (").append(formatDuration(t.trackTimeMillis)).appendLine(")")
        }
        return sb.toString().trimEnd()
    }

    override fun onResume() {
        super.onResume()
        suppressToolbarOnPause = false                 // сброс подавления при возвращении
        (activity as? BaseActivity)
            ?.toolbarHelper
            ?.hideToolbar()    // экран плейлиста без родительского тулбара (залипание в аудио)
    }

    override fun onPause() {
        // чтобы следующий экран начал с «нормального» состояния (или его конфиг перезапишет)
        if (!suppressToolbarOnPause) {
            (activity as? BaseActivity)?.toolbarHelper?.showToolbar()
        }
        super.onPause()
    }
}
