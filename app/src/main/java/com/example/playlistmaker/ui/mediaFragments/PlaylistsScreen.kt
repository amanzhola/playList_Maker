package com.example.playlistmaker.ui.mediaFragments

import android.net.Uri
import android.widget.ImageView
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
            val cols = if (LocalConfiguration.current.smallestScreenWidthDp >= 600) 3 else 2
            LazyVerticalGrid(
                state = gridState, // 👈 ВАЖНО
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
                    key = { it.id } // если у модели другое поле — замени
                ) { playlist ->
                    PlaylistCard(
                        playlist = playlist,
                        onClick = { onOpenPlaylist(playlist.id) }, // поле id при необходимости замени
                        textColor = textColor,
                        cardBackground = Color.Transparent
                    )
                }
            }
        }
    }
}
/** Ждём, пока нужный индекс появится в layoutInfo у грида */
private suspend fun waitUntilItemExists(state: LazyGridState, index: Int) {
    // быстрый выход
    if (state.layoutInfo.totalItemsCount > index) return
    // дать Compose собрать layout хотя бы один кадр
    yield()
    // дождаться, пока общее число элементов станет больше нужного индекса
    snapshotFlow { state.layoutInfo.totalItemsCount }
        .first { it > index }
}
@Composable
fun PlaylistCard(
    playlist: Playlist,
    onClick: () -> Unit,
    textColor: Color,                         // ← внешний цвет текста
    cardBackground: Color = Color.Unspecified // ← внешний фон карточки (если нужен)
) {
    val corner = 8.dp

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(corner))
            .clickable(onClick = onClick)
            .background(cardBackground)
            .padding(8.dp)
    ) {
        // обложка 1:1
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(corner))
        ) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ivCtx ->
                    ImageView(ivCtx).apply {
                        scaleType = ImageView.ScaleType.CENTER_CROP
                        clipToOutline = true
                        background = ContextCompat.getDrawable(ivCtx, R.drawable.rounded_8dp)
                    }
                },
                update = { iv ->
                    val uri: Uri? = playlist.coverPath?.let { ref ->
                        when {
                            ref.startsWith("content://") || ref.startsWith("file://") -> ref.toUri()
                            ref.startsWith("/") -> Uri.fromFile(File(ref))
                            else -> null
                        }
                    }
                    if (uri == null) {
                        iv.setImageResource(R.drawable.placeholder)
                    } else {
                        iv.setImageURI(uri)
                    }
                }
            )
        }

        Spacer(Modifier.height(8.dp))

        Text(
            text = playlist.name,
            color = textColor,
            fontSize = 12.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
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
