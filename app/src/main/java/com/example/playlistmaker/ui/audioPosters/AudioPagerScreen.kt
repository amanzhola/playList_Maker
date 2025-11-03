package com.example.playlistmaker.ui.audioPosters

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.exoplayer.ExoPlayer
import com.example.playlistmaker.domain.models.search.Track
import com.example.playlistmaker.presentation.searchPostersViewModels.ExtraOptionViewModel
import com.example.playlistmaker.ui.audioPosters.adapter.TrackAdapterAudio
import com.example.playlistmaker.ui.audioPosters.adapter.TrackItemCompose
import kotlinx.coroutines.flow.distinctUntilChanged

@Composable
fun AudioPagerScreen(
    viewModel: ExtraOptionViewModel,
    exo: ExoPlayer?,
    initialIndex: Int,                // из VM/аргументов
    videoBoundPosition: Int,          // из VM
    onVideoPosChange: (Int) -> Unit,
    onTogglePlayPauseForCurrent: () -> Unit,
    onBack: () -> Unit,
    onFav: (Track) -> Unit,
    onAdd: () -> Unit,
    onSeek: (Long) -> Unit,
    textColorOverride: androidx.compose.ui.graphics.Color,
    textColorOverrideAux: androidx.compose.ui.graphics.Color,
    iconTintOverride:  androidx.compose.ui.graphics.Color,
    iconTintOverrideAux:  androidx.compose.ui.graphics.Color,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

   // нормализуем стартовый индекс под текущий список
    val normalizedInitial = remember(initialIndex, state.trackList.size) {
        initialIndex.coerceIn(0, (state.trackList.size - 1).coerceAtLeast(0))
    }

    // PagerState создаётся ОДИН раз с нужной стартовой страницей.
    // Дальше он сам сохраняется (rememberSaveable по умолчанию внутри rememberPagerState).
    val pagerState = androidx.compose.foundation.pager.rememberPagerState(
        initialPage = normalizedInitial,
        pageCount = { state.trackList.size }
    )

    // Если размер списка изменился и текущая страница вылетела за границы — мягкая корректировка
    LaunchedEffect(state.trackList.size) {
        val maxPage = (state.trackList.size - 1).coerceAtLeast(0)
        val cur = pagerState.currentPage
        if (cur > maxPage) {
            pagerState.scrollToPage(maxPage)
            viewModel.setCurrentTrackIndex(maxPage)
            viewModel.setScrollPosition(maxPage)
        }
    }

    // Листаем → синхронизируем в VM и, если ушли с видео-страницы, сбрасываем привязку
    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }
            .distinctUntilChanged()
            .collect { page ->
                viewModel.setCurrentTrackIndex(page)
                viewModel.setScrollPosition(page)
            }
    }

    val pageContent: @Composable (Int) -> Unit = { page ->
        val track = state.trackList[page]
        val hasMedia = exo?.currentMediaItem != null
        val isVideoHere = (page == videoBoundPosition && exo != null && hasMedia)

        // 🔑 Никаких AndroidView здесь! Всё через карточку —
        // она сама рисует PlayerView внутри рамки постера.
        val isActiveAudio =
            !isVideoHere && page == state.currentTrackIndex && track.isPlaying

        var ap by remember(page) { mutableStateOf<TrackAdapterAudio.AudioProgress?>(null) }
        LaunchedEffect(page, isActiveAudio) {
            if (!isActiveAudio) { ap = null; return@LaunchedEffect }
            while (true) {
                viewModel.getAudioProgress()?.let { p ->
                    ap = TrackAdapterAudio.AudioProgress(p.positionMs, p.durationMs, p.bufferedMs)
                }
                kotlinx.coroutines.delay(200)
            }
        }

        TrackItemCompose(
            context          = LocalContext.current,
            track            = track,
            isVideoAttached  = isVideoHere,            // ключ
            timebarVertical  = state.isHorizontal,
            currentPlayer    = exo,                    // Player передаём сюда
            overlayPlayTime  = track.playTime,
            audioProgress    = ap,                     // тикает только на активной аудио-странице
            onPlayToggle     = { onTogglePlayPauseForCurrent() },
            onItemClick      = {
                viewModel.setCurrentTrackIndex(page)
                viewModel.toggleIsHorizontal()
                viewModel.setScrollPosition(page)
            },
            onBack = onBack,
            onFav  = { onFav(track) },
            onAdd  = onAdd,
            onSeek = onSeek,
            style  = TrackAdapterAudio.ItemStyle.TRACK_ITEM_1,
            textColorOverride = textColorOverride,
            textColorOverrideAux = textColorOverrideAux,
            iconTintOverride  = iconTintOverride,
            iconTintOverrideAux  = iconTintOverrideAux,
        )
    }

    if (state.isHorizontal) {
        androidx.compose.foundation.pager.HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page -> pageContent(page) }
    } else {
        androidx.compose.foundation.pager.VerticalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page -> pageContent(page) }
    }
}
