package com.example.playlistmaker.ui.media

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import com.example.playlistmaker.R
import com.example.playlistmaker.domain.models.search.Track
import com.example.playlistmaker.ui.mediaFragments.FavouriteTracksScreen
import com.example.playlistmaker.ui.mediaFragments.PlaylistsScreen
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

@Composable
fun MediaLibraryScreen(
    startPage: Int,
    onPageChanged: (Int) -> Unit,
    onOpenPlaylist: (Long) -> Unit,
    onOpenTrack: (Track, String, Int) -> Unit,
    onCreatePlaylist: () -> Unit,
    // цвета-оверрайды
    favScreenBackgroundOverride: Color? = null,
    favRowTextColorOverride: Color? = null,
    favRowIconColorOverride: Color? = null,
    favRowBackgroundOverride: Color? = null,
    playlistsScreenBackgroundOverride: Color? = null,
    playlistsCardTextColorOverride: Color? = null,
    // прокрутка для вкладки Playlists
    playlistsScrollToId: Flow<Long>,
    playlistsScrollTop:  Flow<Unit>
) {
    val titles = listOf(R.string.favourites, R.string.playlists)
    // сохраняем позицию пейджера
    val pagerState = rememberPagerState(
        initialPage = startPage.coerceIn(0, titles.size - 1),
        pageCount = { titles.size }
    )
    val scope = rememberCoroutineScope()

    // уведомляем фрагмент при смене
    LaunchedEffect(pagerState.currentPage) {
        onPageChanged(pagerState.currentPage)
    }

    Column(Modifier.fillMaxSize()) {
        MediaTabsBarPure(
            selectedIndex = pagerState.currentPage,
            titlesRes = titles,
            onTabClick = { i -> scope.launch { pagerState.animateScrollToPage(i) } }
        )

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f)
        ) { page ->
            when (page) {
                0 -> FavouriteTracksScreen(
                    onOpenTrack = onOpenTrack,
                    screenBackgroundOverride = favScreenBackgroundOverride
                        ?: colorResource(R.color.white_textColor),
                    rowTextColorOverride  = favRowTextColorOverride,
                    rowIconColorOverride  = favRowIconColorOverride,
                    rowBackgroundOverride = favRowBackgroundOverride
                )
                1 -> PlaylistsScreen(
                    onOpenPlaylist = onOpenPlaylist,
                    onCreatePlaylist = onCreatePlaylist,
                    backgroundColor = playlistsScreenBackgroundOverride
                        ?: colorResource(R.color.white_textColor),
                    textColor = playlistsCardTextColorOverride
                        ?: colorResource(R.color.textColor_white),
                    // два сигнала прокрутки:
                    scrollToIdFlow = playlistsScrollToId,
                    scrollTopFlow  = playlistsScrollTop,
                    // сохраняем позицию грида между переходами
                    gridState = rememberSaveable(saver = LazyGridState.Saver) { LazyGridState() }
                )
            }
        }
    }
}
