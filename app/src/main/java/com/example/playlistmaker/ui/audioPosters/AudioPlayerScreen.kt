package com.example.playlistmaker.ui.audioPosters

import android.content.res.Configuration
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.Player
import com.example.playlistmaker.BaseActivity
import com.example.playlistmaker.R
import com.example.playlistmaker.presentation.searchPostersViewModels.ExtraOptionViewModel
import com.example.playlistmaker.presentation.utils.darken
import com.example.playlistmaker.presentation.utils.lighten
import com.example.playlistmaker.ui.audioPosters.sheet.PlaylistsSheet
import com.example.playlistmaker.utils.NO_VIDEO_POSITION
import com.example.playlistmaker.utils.findActivity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AudioPlayerScreen(
    viewModel: ExtraOptionViewModel,
    imageLoader: com.example.playlistmaker.presentation.ImageLoader,
    onBack: () -> Unit,
    onCreatePlaylistClick: () -> Unit,
    onTogglePlayPauseForCurrent: () -> Unit,
    onSeek: (Long) -> Unit,
    extBackground: androidx.compose.ui.graphics.Color? = null,
    extTextColor:  androidx.compose.ui.graphics.Color? = null,
    extIconTint:   androidx.compose.ui.graphics.Color? = null,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    // базовые цвета как
    val defaultBg   = colorResource(R.color.white_textColor)
    val defaultText = colorResource(R.color.textColor_white)
    val defaultIcon = colorResource(R.color.textColor_white)

    val bg   = extBackground ?: defaultBg
    val tCol = extTextColor  ?: defaultText
    val tColaux = if (defaultText.luminance() > 0.5f) {
        tCol.darken(0.35f)
    } else {
        tCol.lighten(0.35f)
    }
    val iCol = extIconTint   ?: defaultIcon
    val iColaux = if (defaultIcon.luminance() > 0.5f) {
        iCol.darken(0.45f)
    } else {
        iCol.lighten(0.45f)
    }

    val videoBound by viewModel.videoPosFlow.collectAsStateWithLifecycle()
    val exo        by viewModel.exoFlow.collectAsStateWithLifecycle()

    // 👇 вычисляем fullscreen
    val cfg = LocalConfiguration.current
    val isLandscape = cfg.orientation == Configuration.ORIENTATION_LANDSCAPE
    val hasVideo = exo?.currentMediaItem != null && videoBound != NO_VIDEO_POSITION
    val fullscreen = isLandscape && hasVideo &&
            (exo?.isPlaying == true || exo?.playbackState == Player.STATE_READY)

    // >>> Utube:
    val activity = (LocalContext.current.findActivity() as? BaseActivity)
    DisposableEffect(fullscreen) {
        if (fullscreen) {
            activity?.toolbarHelper?.hideForFullscreen()
            activity?.enterVideoFullscreenUi()
        } else {
            activity?.exitVideoFullscreenUi()
            activity?.toolbarHelper?.restoreAfterFullscreen()
        }
        onDispose {
            // страховка при уходе со скрина/повторной компоновке
            activity?.exitVideoFullscreenUi()
            activity?.toolbarHelper?.restoreAfterFullscreen()
        }
    }

    LaunchedEffect(videoBound) {
        Log.d("VIDEO_BOUND", "AudioPlayerScreen: videoBound (from VM) = $videoBound")
    }

    val playlists by viewModel.playlists.collectAsStateWithLifecycle(initialValue = emptyList())
    val snackbarHostState = remember { SnackbarHostState() }

    val sheetBg = colorResource(R.color.white_textColor)            // заменяем цвет шторки, если нужно
    val scrim = colorResource(R.color.black).copy(alpha = 0.6f)
    // полупрозрачный фон; свой ресурс

    var isSheetOpen by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)

    // Snack-и из VM (раньше были во фрагменте)
    LaunchedEffect(Unit) {
        viewModel.playlistEvents.collect { e ->
            val msg = when (e) {
                is ExtraOptionViewModel.PlaylistEvent.Added ->
                    "Добавлено в плейлист: ${e.playlistName}"
                is ExtraOptionViewModel.PlaylistEvent.AlreadyExists ->
                    "Трек уже в плейлисте: ${e.playlistName}"
                is ExtraOptionViewModel.PlaylistEvent.Error ->
                    e.message
            }
            snackbarHostState.showSnackbar(message = msg)
        }
    }

    Scaffold(
        containerColor = bg,
        topBar = { /* тулбар родительский */ },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { padding ->
        Box(
            Modifier
                .fillMaxSize()
                .background(bg)
            .padding(padding)
            ) {
            AudioPagerScreen(
                viewModel = viewModel,
                exo = exo,
                initialIndex = state.currentTrackIndex,
                videoBoundPosition = videoBound,
                onVideoPosChange = { viewModel.videoPos = it },
                onTogglePlayPauseForCurrent = onTogglePlayPauseForCurrent,
                onBack = onBack,
                onFav = { track -> viewModel.onFavoriteClicked(track.id) },
                onAdd = {
                    viewModel.onOpenBottomSheet()
                    isSheetOpen = true
                },
                onSeek = onSeek,
                textColorOverride = tCol,
                textColorOverrideAux = tColaux,
                iconTintOverride  = iCol,
                iconTintOverrideAux  = iColaux,
                forceFullscreen = fullscreen
            )
        }
    }

    if (isSheetOpen) {
        ModalBottomSheet(
            onDismissRequest = { isSheetOpen = false },
            sheetState = sheetState,
            containerColor = sheetBg,
            scrimColor = scrim,
            dragHandle = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = dimensionResource(R.dimen.searchPaddingTopBottom_8)),
                    contentAlignment = Alignment.TopCenter
                ) {
                    // Сам «язычок»
                    Box(
                        modifier = Modifier
                            .width(50.dp) // как <size android:width="50dp"/>
                            .height(dimensionResource(R.dimen.marginBottom_4)) // как в shape
                            .background(
                                color = colorResource(R.color.hintField_white), // нужный цвет
                                shape = RoundedCornerShape(44.dp)              // как <corners android:radius="44dp"/>
                            )
                    )
                }
            }
        ) {
            PlaylistsSheet(
                playlists = playlists,
                imageLoader = imageLoader,
                onPlaylistClick = { pl ->
                    viewModel.onPlaylistClicked(pl)
                    isSheetOpen = false
                },
                onCreatePlaylistClick = {
                    isSheetOpen = false
                    onCreatePlaylistClick()
                }
            )
            Spacer(Modifier.height(12.dp))
        }
    }
}
