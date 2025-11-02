@file:Suppress("FunctionName")

package com.example.playlistmaker.ui.playlistInfo.compose

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetValue.Hidden
import androidx.compose.material3.SheetValue.PartiallyExpanded
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.boundsInParent
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.integerResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import coil3.compose.rememberAsyncImagePainter
import com.example.playlistmaker.R
import com.example.playlistmaker.domain.models.search.Track
import com.example.playlistmaker.presentation.playlistInfo.PlaylistInfoViewModel
import com.example.playlistmaker.ui.audioPosters.adapter.LegacyTextStyles
import com.example.playlistmaker.ui.playlistInfo.model.MenuRow
import com.example.playlistmaker.utils.FailBlock
import com.example.playlistmaker.utils.FailTextPlacement
import com.example.playlistmaker.utils.TrackRow
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistInfoScreen(
    ui: PlaylistInfoViewModel.Ui,
    onBack: () -> Unit,
    onShare: () -> Unit,
    onEdit: () -> Unit,
    onDeletePlaylist: () -> Unit,
    onTrackClick: (tracks: List<Track>, index: Int) -> Unit,
    onTrackLongClick: (Track) -> Unit,
) {
    val cfg = LocalConfiguration.current
    val isLandscape = cfg.orientation == Configuration.ORIENTATION_LANDSCAPE

    val extraBelowShare = 3.dp
    val paddingH = dimensionResource(R.dimen.Padding_16)
    val screenBg = colorResource(R.color.white1)

    // --- Меню (вторая шторка) ---
    var menuOpen by rememberSaveable { mutableStateOf(false) }
    val menuSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // обложка
    val coverPainter = rememberAsyncImagePainter(model = ui.coverPath ?: R.drawable.placeholder2)

    if (isLandscape) {
        // ───────── LANDSCAPE: слева 40% обложка, справа 60% колонка с локальными шторками ─────────
        Row(
            modifier = Modifier
                .fillMaxSize()
                .background(screenBg)
                .systemBarsPadding()
        ) {
            // Левая 40% — обложка
            Box(
                modifier = Modifier
                    .weight(0.4f)
                    .fillMaxHeight()
            ) {
                Image(
                    painter = coverPainter,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .padding(12.dp)
                        .align(Alignment.TopStart)
                        .size(48.dp)
                ) {
                    Icon(
                        painterResource(R.drawable.arrow_back),
                        contentDescription = stringResource(R.string.arrow_back),
                        tint = colorResource(R.color.black1)
                    )
                }
            }

            // Правая 60% — ВЕСЬ контент + 2 правые шторки (локально внутри правой половины)
            RightPaneWithSheets(
                ui = ui,
                paddingH = paddingH,
                extraBelowShare = extraBelowShare,
                onShare = onShare,
                onOpenMenu = { menuOpen = true },
                onEdit = onEdit,
                onDeletePlaylist = onDeletePlaylist,
                onTrackClick = onTrackClick,
                onTrackLongClick = onTrackLongClick,
                menuOpen = menuOpen,
                onCloseMenu = { menuOpen = false },
                modifier = Modifier.weight(0.6f)
            )
        }
    } else {
        // ───────── PORTRAIT: как раньше (основная шторка на всю ширину) ─────────
        PortraitWithSheets(
            ui = ui,
            coverPainter = coverPainter,
            paddingH = paddingH,
            extraBelowShare = extraBelowShare,
            onBack = onBack,
            onShare = onShare,
            onOpenMenu = { menuOpen = true },
            onTrackClick = onTrackClick,
            onTrackLongClick = onTrackLongClick
        )

        // Меню — обычный ModalBottomSheet на всю ширину (как раньше)
        if (menuOpen) {
            ModalBottomSheet(
                onDismissRequest = { menuOpen = false },
                containerColor = colorResource(R.color.white_textColor),
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                sheetState = menuSheetState,
                scrimColor = Color.Black.copy(alpha = 0.32f),
            ) {
                MenuSheetContent(
                    header = MenuRow.Header(
                        cover = ui.coverPath,
                        name = ui.name,
                        count = pluralStringResource(R.plurals.tracks_count, ui.tracksCount, ui.tracksCount)
                    ),
                    onShare = { menuOpen = false; onShare() },
                    onEdit  = { menuOpen = false; onEdit() },
                    onDelete = { menuOpen = false; onDeletePlaylist() }
                )
            }
        }
    }
}

/* ====================== LANDSCAPE: правая колонка со шторками ====================== */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RightPaneWithSheets(
    ui: PlaylistInfoViewModel.Ui,
    paddingH: Dp,
    extraBelowShare: Dp,
    onShare: () -> Unit,
    onOpenMenu: () -> Unit,
    onEdit: () -> Unit,
    onDeletePlaylist: () -> Unit,
    onTrackClick: (tracks: List<Track>, index: Int) -> Unit,
    onTrackLongClick: (Track) -> Unit,
    menuOpen: Boolean,
    onCloseMenu: () -> Unit,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current

    var rightRootHeightPx by remember { mutableIntStateOf(0) }
    var shareBottomPx     by remember { mutableIntStateOf(0) }

    // локальная шторка только для правой половины
    val tracksSheetState = rememberStandardBottomSheetState(
        initialValue = PartiallyExpanded,
        skipHiddenState = true,
        confirmValueChange = { it != Hidden }
    )
    val scaffoldState = rememberBottomSheetScaffoldState(bottomSheetState = tracksSheetState)

    val peekDp = with(density)

    {
        val extraPx    = extraBelowShare.roundToPx()
        val desiredTop = shareBottomPx + extraPx
        val peekPx     = (rightRootHeightPx - desiredTop).coerceAtLeast(1)
        peekPx.toDp()
    }

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        // 1) ШТОРКА: РИСУЕМ СЗАДИ (прозрачный контейнер), чтобы не перекрывала текст сверху
        BottomSheetScaffold(
            modifier = Modifier
                .matchParentSize()
                .zIndex(2f),                                        // ниже текста
            scaffoldState = scaffoldState,
            sheetPeekHeight = peekDp,
            containerColor = Color.Transparent,                     // КРИТИЧНО: не закрашивать фон
            sheetContainerColor = colorResource(R.color.white_textColor),
            sheetShape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
            sheetContent = {
                Column(Modifier.fillMaxHeight()) {
                    if (ui.tracks.isEmpty()) {
                        FailBlock(
                            textId = R.string.no_tracks_in_playlist,
                            enabled = true,
                            textPlacement = FailTextPlacement.End,
                            topPadding = 10.dp
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(bottom = 16.dp)
                        ) {
                            itemsIndexed(ui.tracks, key = { _, t -> t.trackId }) { i, t ->
                                TrackRow(
                                    track = t,
                                    onClick = { onTrackClick(ui.tracks, i) },
                                    onRemove = {},
                                    onLongClick = { onTrackLongClick(t) },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }
            }
        ) {
            // Тело scaffold не нужно — оставляем пустым
            Box(Modifier.fillMaxSize())
        }

        // 2) ПРАВАЯ ИНФО-КОЛОНКА: РИСУЕМ ВПЕРЕДИ, чтобы её верх был всегда виден над шторкой
        Column(
            modifier = Modifier
                .matchParentSize()
                .zIndex(1f)
                .background(Color(0xFFF2F3F5))// ← выше шторки
                .padding(horizontal = paddingH, vertical = 16.dp)
                .onGloballyPositioned { coords ->
                    rightRootHeightPx = coords.size.height          // высота правой половины
                },
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                ui.name,
                style = LegacyTextStyles.text24_700(),
                color = colorResource(R.color.black1),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            ui.description?.takeIf { it.isNotBlank() }?.let {
                Spacer(Modifier.height(dimensionResource(R.dimen.album_margin_top)))
                Text(
                    it,
                    style = LegacyTextStyles.text18_400(),
                    color = colorResource(R.color.black1),
                    maxLines = integerResource(R.integer.qty_lines_create_playlist),
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(Modifier.height(8.dp))
            val minutesText = pluralStringResource(
                R.plurals.minutes_count, ui.minutesTotal.toInt(), ui.minutesTotal
            )
            val tracksText = pluralStringResource(
                R.plurals.tracks_count, ui.tracksCount, ui.tracksCount
            )
            Text(
                stringResource(R.string.two_parts_with_dot, minutesText, tracksText),
                color = colorResource(R.color.black1),
                style = LegacyTextStyles.text18_400()
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .onGloballyPositioned { c ->
                        // низ ряда в координатах правой колонки
                        shareBottomPx = c.boundsInParent().bottom.roundToInt()
                    }
                    .padding(top = dimensionResource(R.dimen.album_margin_top)),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onShare) {
                    Icon(
                        painterResource(R.drawable.share),
                        contentDescription = stringResource(R.string.share),
                        tint = colorResource(R.color.black1)
                    )
                }
                Spacer(Modifier.width(16.dp))
                IconButton(onClick = onOpenMenu) {
                    Icon(
                        painterResource(R.drawable.dot_3),
                        contentDescription = stringResource(R.string.menu),
                        tint = colorResource(R.color.black1)
                    )
                }
            }
        }

        // Правая «меню-шторка» — теперь это BottomSheetScaffold с drag handle
        if (menuOpen) {
            // затемнение только над правой половиной (клик по фону закрывает)
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .zIndex(3f)
                    .background(Color.Black.copy(alpha = 0.32f))
                    .clickable { onCloseMenu() }
            )

            // локальное состояние меню-листа
            val menuBottomState = rememberStandardBottomSheetState(
                initialValue = PartiallyExpanded,   // начнём с частично открытой
                skipHiddenState = true,
                confirmValueChange = { it != Hidden }
            )
            val menuScaffold = rememberBottomSheetScaffoldState(bottomSheetState = menuBottomState)

            BottomSheetScaffold(
                modifier = Modifier
                    .matchParentSize()
                    .zIndex(4f), // меню поверх затемнения
                scaffoldState = menuScaffold,
                sheetPeekHeight = peekDp, // поднятие для ландскейпа до уровня первой (sheetPeekHeight = 64.dp,)
                containerColor = Color.Transparent, // фон вокруг листа не закрашиваем
                sheetContainerColor = colorResource(R.color.white_textColor),
                sheetShape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                // 👇 добавляем «усик»
                sheetDragHandle = { BottomSheetDefaults.DragHandle() },
                sheetContent = {
                    Column(
                        modifier = Modifier
                            .fillMaxHeight()
                            .padding(16.dp)
                    ) {
                        MenuSheetContent(
                            header = MenuRow.Header(
                                cover = ui.coverPath,
                                name = ui.name,
                                count = pluralStringResource(R.plurals.tracks_count, ui.tracksCount, ui.tracksCount)
                            ),
                            onShare  = { onCloseMenu(); onShare() },
                            onEdit   = { onCloseMenu(); onEdit() },
                            onDelete = { onCloseMenu(); onDeletePlaylist() }
                        )
                        Spacer(Modifier.height(8.dp))
                    }
                }
            ) {
                Box(Modifier.fillMaxSize())
            }
        }

    }
}

/* ====================== PORTRAIT: прежнее поведение ====================== */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PortraitWithSheets(
    ui: PlaylistInfoViewModel.Ui,
    coverPainter: Any, // rememberAsyncImagePainter выше уже создан
    paddingH: Dp,
    extraBelowShare: Dp,
    onBack: () -> Unit,
    onShare: () -> Unit,
    onOpenMenu: () -> Unit,
    onTrackClick: (tracks: List<Track>, index: Int) -> Unit,
    onTrackLongClick: (Track) -> Unit,
) {
    val density = LocalDensity.current
    var rootHeightPx by remember { mutableStateOf(0) }
    var shareBottomPx by remember { mutableStateOf(0) }

    val tracksSheetState = rememberStandardBottomSheetState(
        initialValue = PartiallyExpanded,
        skipHiddenState = true,
        confirmValueChange = { it != Hidden }
    )
    val scaffoldState = rememberBottomSheetScaffoldState(bottomSheetState = tracksSheetState)

    val peekDp = with(density) {
        val extraPx = extraBelowShare.roundToPx()
        val desiredTop = shareBottomPx + extraPx
        val peekPx = (rootHeightPx - desiredTop).coerceAtLeast(1)
        peekPx.toDp()
    }

    BottomSheetScaffold(
        modifier = Modifier
            .fillMaxSize()
            .onGloballyPositioned { coords ->
                rootHeightPx = coords.boundsInRoot().height.roundToInt()
            }
            .background(colorResource(R.color.white1))
            .systemBarsPadding(),
        scaffoldState = scaffoldState,
        sheetPeekHeight = peekDp,
        sheetContainerColor = colorResource(R.color.white_textColor),
        sheetShape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        sheetContent = {
            Column(Modifier.fillMaxHeight()) {
                if (ui.tracks.isEmpty()) {
                    FailBlock(
                        textId = R.string.no_tracks_in_playlist,
                        enabled = true,
                        textPlacement = FailTextPlacement.End,
                        topPadding = 10.dp
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(bottom = 16.dp)
                    ) {
                        itemsIndexed(ui.tracks, key = { _, t -> t.trackId }) { i, t ->
                            TrackRow(
                                track = t,
                                onClick = { onTrackClick(ui.tracks, i) },
                                onRemove = {},
                                onLongClick = { onTrackLongClick(t) },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Кавер (1:1)
            Box {
                Image(
                    painter = coverPainter as androidx.compose.ui.graphics.painter.Painter,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                )
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .padding(12.dp)
                        .align(Alignment.TopStart)
                        .size(48.dp)
                ) {
                    Icon(
                        painterResource(R.drawable.arrow_back),
                        contentDescription = stringResource(R.string.arrow_back),
                        tint = colorResource(R.color.black1)
                    )
                }
            }

            // Инфо-панель
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF2F3F5)) // 👈 фон темнее белого
                    .padding(horizontal = paddingH, vertical = 16.dp)
            ) {
                Text(
                    ui.name,
                    style = LegacyTextStyles.text24_700(),
                    color = colorResource(R.color.black1),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                ui.description?.takeIf { it.isNotBlank() }?.let {
                    Spacer(Modifier.height(dimensionResource(R.dimen.album_margin_top)))
                    Text(
                        it,
                        style = LegacyTextStyles.text18_400(),
                        color = colorResource(R.color.black1),
                        maxLines = integerResource(R.integer.qty_lines_create_playlist),
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(Modifier.height(8.dp))
                val minutesText = pluralStringResource(
                    R.plurals.minutes_count, ui.minutesTotal.toInt(), ui.minutesTotal
                )
                val tracksText = pluralStringResource(
                    R.plurals.tracks_count, ui.tracksCount, ui.tracksCount
                )
                Text(
                    stringResource(R.string.two_parts_with_dot, minutesText, tracksText),
                    color = colorResource(R.color.black1),
                    style = LegacyTextStyles.text18_400()
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .onGloballyPositioned { c ->
                            shareBottomPx = c.boundsInRoot().bottom.roundToInt()
                        }
                        .padding(top = dimensionResource(R.dimen.album_margin_top)),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onShare) {
                        Icon(
                            painterResource(R.drawable.share),
                            contentDescription = stringResource(R.string.share),
                            tint = colorResource(R.color.black1)
                        )
                    }
                    Spacer(Modifier.width(16.dp))
                    IconButton(onClick = onOpenMenu) {
                        Icon(
                            painterResource(R.drawable.dot_3),
                            contentDescription = stringResource(R.string.menu),
                            tint = colorResource(R.color.black1)
                        )
                    }
                }
            }
        }
    }
}

/* ====================== общее: меню-контент и экшены ====================== */

@Composable
private fun MenuSheetContent(
    header: MenuRow.Header,
    onShare: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val painter = rememberAsyncImagePainter(model = header.cover ?: R.drawable.placeholder)
    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {

        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painter, contentDescription = null, contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(45.dp)
                    .clip(RoundedCornerShape(4.dp))
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(header.name, style = LegacyTextStyles.text16_400(), color = colorResource(R.color.textColor_white))
                Text(header.count, style = LegacyTextStyles.text11_400(), color = colorResource(R.color.hintColor_white))
            }
        }
        Spacer(Modifier.height(16.dp))
        SheetAction(text = stringResource(R.string.share), onClick = onShare)
        SheetAction(text = stringResource(R.string.edit), onClick = onEdit)
        SheetAction(text = stringResource(R.string.delete_playlist), onClick = onDelete)
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun SheetAction(text: String, onClick: () -> Unit) {
    Text(
        text = text,
        style = LegacyTextStyles.text16_400(),
        color = colorResource(R.color.textColor_white),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp)
    )
}
