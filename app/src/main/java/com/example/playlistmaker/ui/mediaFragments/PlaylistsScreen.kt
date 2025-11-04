package com.example.playlistmaker.ui.mediaFragments

import android.annotation.SuppressLint
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import coil3.size.Precision
import coil3.size.Scale
import com.example.playlistmaker.R
import com.example.playlistmaker.domain.models.playlist.Playlist
import com.example.playlistmaker.presentation.media.PlaylistViewModel
import com.example.playlistmaker.utils.FailBlock
import com.example.playlistmaker.utils.FailTextPlacement
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.yield
import org.koin.androidx.compose.koinViewModel
import java.io.File

@SuppressLint("ConfigurationScreenWidthHeight")
@Composable
fun PlaylistsScreen(
    vm: PlaylistViewModel = koinViewModel(),
    onOpenPlaylist: (Long) -> Unit,
    onCreatePlaylist: () -> Unit,
    backgroundColor: Color,
    textColor: Color,
    scrollToIdFlow: Flow<Long>? = null,    // для возврата из Info
    scrollTopFlow:  Flow<Unit>? = null,    // для возврата после Create
    gridState: LazyGridState = rememberLazyGridState(),
) {
    val list by vm.playlists.collectAsStateWithLifecycle()

    // 🔧 анти-мигание: не показываем промежуточный пустой список
    var lastNonEmpty by remember { mutableStateOf(emptyList<Playlist>()) }
    val display = if (list.isEmpty() && lastNonEmpty.isNotEmpty()) lastNonEmpty else list
    if (list.isNotEmpty()) lastNonEmpty = list

    // таргетный размер превью в пикселях (фикс для всех карточек)
    val cfg = LocalConfiguration.current
    val density = LocalResources.current.displayMetrics.density
    val cols = if (cfg.smallestScreenWidthDp >= 600) 3 else 2
    val screenDp = cfg.screenWidthDp
    val horizontalPaddingDp = 16     // как в LazyVerticalGrid: contentPadding(8.dp) по краям -> 16dp суммарно
    val itemSpacingDp = 8            // как в LazyVerticalGrid: horizontalArrangement = 8.dp
    val cellDp = ((screenDp - horizontalPaddingDp * 2 - itemSpacingDp * (cols - 1)) / cols)
    val cellPx = (cellDp * density).toInt().coerceAtLeast(64)

    // НОВОЕ: храним последнюю цель скролла, чтобы не потерять её между кадрами
    var targetId by remember { mutableStateOf<Long?>(null) }

    // приход из Info
    LaunchedEffect(scrollToIdFlow) {
        if (scrollToIdFlow == null) return@LaunchedEffect
        scrollToIdFlow.collect { id -> targetId = id }
    }

    // приход из Create → вверх
    LaunchedEffect(scrollTopFlow) {
        if (scrollTopFlow == null) return@LaunchedEffect
        scrollTopFlow.collect {
            waitUntilItemExists(gridState, 0)
            gridState.scrollToItem(0)
            targetId = null
        }
    }

    // выполнить «скролл к id», когда данные готовы
    LaunchedEffect(targetId, display) {
        val id = targetId ?: return@LaunchedEffect
        if (display.isEmpty()) return@LaunchedEffect
        val idx = display.indexOfFirst { it.id == id }
        if (idx >= 0) {
            waitUntilItemExists(gridState, idx)
            gridState.scrollToItem(idx)
            targetId = null
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(dimensionResource(R.dimen.text_size_small)))

        Button(
            onClick = onCreatePlaylist,
            shape = RoundedCornerShape(dimensionResource(R.dimen.track_45)),
            colors = ButtonDefaults.buttonColors(
                containerColor = textColor,
                contentColor = backgroundColor
            ),
            modifier = Modifier
                .padding(
                    start = dimensionResource(R.dimen.Padding_16),
                    end   = dimensionResource(R.dimen.Padding_16),
                    top   = dimensionResource(R.dimen.searchPaddingTopBottom_8)
                )
                .defaultMinSize(minHeight = dimensionResource(R.dimen.searchLineHeight_52))
        ) {
            Text(
                text = stringResource(R.string.newPlaylist),
                fontSize = 14.sp,
                fontFamily = FontFamily(Font(R.font.ys_display_medium)),
                fontWeight = FontWeight.Medium
            )
        }

        if (display.isEmpty()) {
            // пусто только когда действительно пусто и «кеша» нет
            FailBlock(
                textId = R.string.noPlayList,
                enabled = true,
                textPlacement = FailTextPlacement.Top
            )
        } else {
            LazyVerticalGrid(
                state = gridState,
                columns = GridCells.Fixed(cols),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(
                    items = display,
                    key = { it.id },
                    contentType = { "playlist" } // стабильный тип
                ) { playlist ->
                    PlaylistCard(
                        playlist = playlist,
                        onClick = { onOpenPlaylist(playlist.id) },
                        textColor = textColor,
                        cardBackground = backgroundColor, // или Color.Transparent
                        targetPx = cellPx
                    )
                }
            }
        }
    }
}

/** Ждём, пока нужный индекс появится в layoutInfo у грида */
private suspend fun waitUntilItemExists(state: LazyGridState, index: Int) {
    if (state.layoutInfo.totalItemsCount > index) return
    yield()
    snapshotFlow { state.layoutInfo.totalItemsCount }
        .first { it > index }
}

@Composable
fun PlaylistCard(
    playlist: Playlist,
    onClick: () -> Unit,
    textColor: Color,
    cardBackground: Color = Color.Unspecified,
    targetPx: Int
) {
    val corner = 8.dp
    val context = LocalContext.current

    val uri: Uri? = remember(playlist.coverPath) {
        playlist.coverPath?.let { ref ->
            when {
                ref.startsWith("content://") || ref.startsWith("file://") -> ref.toUri()
                ref.startsWith("/") -> Uri.fromFile(File(ref))
                else -> null
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(corner))
            .clickable(onClick = onClick)
            .background(cardBackground)
            .padding(8.dp)
    ) {
        // Обложка 1:1
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(corner)),
            contentAlignment = Alignment.Center
        ) {
            val request = remember(uri, targetPx) {
                ImageRequest.Builder(context)
                    .data(uri ?: R.drawable.placeholder)
                    .crossfade(false)               // без анимаций = меньше лагов
                    .precision(Precision.INEXACT)   // даунсемплим грубо — ок для превью
                    .scale(Scale.FILL)
                    .size(targetPx, targetPx)       // КЛЮЧ: фиксированный таргет для всех карточек
                    // .bitmapConfig(Bitmap.Config.RGB_565) // на слабых устройствах
                    .build()
            }

            AsyncImage(
                model = request,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                error = painterResource(R.drawable.placeholder),
                modifier = Modifier.fillMaxSize()
            )
        }

        Spacer(Modifier.height(8.dp))

        Text(
            text = playlist.name,
            color = textColor,
            fontSize = 12.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            fontFamily = FontFamily(Font(R.font.ys_display_medium)),
            fontWeight = FontWeight.Medium
        )

        Spacer(Modifier.height(4.dp))

        val cnt = playlist.tracksCount
        val plural = pluralStringResource(R.plurals.tracks_count, cnt, cnt)
        Text(
            text = plural,
            color = textColor.copy(alpha = 0.8f),
            fontSize = 12.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
