package com.example.playlistmaker.ui.audio.compose

<<<<<<< Updated upstream
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.FocusInteraction
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
=======
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.FocusInteraction
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
>>>>>>> Stashed changes
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
<<<<<<< Updated upstream
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
=======
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
>>>>>>> Stashed changes
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
<<<<<<< Updated upstream
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
=======
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalDensity
>>>>>>> Stashed changes
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
<<<<<<< Updated upstream
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
=======
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
>>>>>>> Stashed changes
import com.example.playlistmaker.R
import com.example.playlistmaker.domain.models.search.Track
import com.example.playlistmaker.presentation.searchViewModels.ErrorState
import com.example.playlistmaker.presentation.searchViewModels.models.SearchUiState
<<<<<<< Updated upstream
import com.example.playlistmaker.presentation.utils.deriveFieldBgFromScreen
import com.example.playlistmaker.utils.FailBlock
import com.example.playlistmaker.utils.TrackRow
import com.example.playlistmaker.utils.UpdateButton
import kotlinx.coroutines.flow.collectLatest

@Composable
fun SearchScreen(
    modifier: Modifier = Modifier,
=======
import kotlinx.coroutines.launch

@Composable
fun SearchScreen(
>>>>>>> Stashed changes
    state: SearchUiState,
    onQueryChange: (String) -> Unit,
    onClear: () -> Unit,
    onRetry: () -> Unit,
    onHistoryClear: () -> Unit,
    onItemClick: (Track) -> Unit,
    onRemoveClick: (Track) -> Unit,
<<<<<<< Updated upstream
    onFocusChanged: (Boolean) -> Unit,
    screenBackgroundOverride: Color? = null,
    rowTextColorOverride: Color? = null,
    rowIconColorOverride: Color? = null,
    rowBackgroundOverride: Color? = null,
    scrollToTopFlow: kotlinx.coroutines.flow.Flow<Unit>? = null  // НОВОЕ:
) {
    val listState = rememberLazyListState()
    val bg = screenBackgroundOverride ?: colorResource(R.color.white_textColor)

    val fieldBgForSearch =
        screenBackgroundOverride?.let { deriveFieldBgFromScreen(it) }

    // НОВОЕ: ловим сигнал и скроллим
    LaunchedEffect(scrollToTopFlow, listState) {
        scrollToTopFlow?.let { flow ->
            flow.collectLatest {
                // даём Compose применить изменения списка
                withFrameNanos { /* один кадр ожидания */ }
                // а теперь прокручиваем к началу (можно animateScrollToItem, если нужна анимация)
                listState.scrollToItem(0)
            }
        }
    }

    Column(
        modifier
=======
    onFocusChanged: (Boolean) -> Unit
) {
    val listState = rememberLazyListState()

    // Прокрутка к началу при возврате на экран
    val scope = rememberCoroutineScope()
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        scope.launch {
            listState.scrollToItem(0)
        }
    }

    val bg = colorResource(R.color.white_textColor)

    Column(
        Modifier
>>>>>>> Stashed changes
            .fillMaxSize()
            .background(bg)
    ) {
        SearchInput(
            state = state,
            onQueryChange = onQueryChange,
            onClear = onClear,
<<<<<<< Updated upstream
            onFocusChanged = onFocusChanged,
            fieldBgOverride = fieldBgForSearch
=======
            onFocusChanged = onFocusChanged
>>>>>>> Stashed changes
        )
        // Заголовок "История"
        if (state.showHistory) {
            Text(
                text = stringResource(R.string.search_history),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        top = dimensionResource(R.dimen.arrow_back_24),
                        bottom = dimensionResource(R.dimen.searchPaddingTopBottom_8)
                    ),
                textAlign = TextAlign.Center,
                color = colorResource(R.color.black_white),
                fontSize = dimensionResource(R.dimen.Settings_Text_16).value.sp,
                fontWeight = FontWeight.Medium
            )
        }
        // Прогресс
        if (state.query.isNotBlank() && state.isLoading) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .padding(
                        top = dimensionResource(R.dimen.track_45),
                        bottom = dimensionResource(R.dimen.Padding_16)
                    ),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = colorResource(R.color.blue))
            }
        }
        // Ошибки
        val hasError = state.error != ErrorState.NONE
        when (state.error) {
            ErrorState.ERROR   -> FailBlock(R.string.searchFail, enabled = true)
            ErrorState.FAILURE -> FailBlock(R.string.networkFail, enabled = false)
            ErrorState.NONE    -> Unit
        }
        // Флаги кнопки
        val showClean  = state.error == ErrorState.NONE && state.showHistory
        val showUpdate = state.error == ErrorState.FAILURE
        val showBottomButton = showClean || showUpdate

        // Нижний «контейнер»: список + кнопка.
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f) // занимает остаток экрана
        ) {
            // высота, которую нужно зарезервировать под кнопку
            val btnHeight = if (showBottomButton) dimensionResource(R.dimen.searchLineHeight_52) else 0.dp
            val btnVPad   = if (showBottomButton) dimensionResource(R.dimen.searchPaddingTopBottom_8) else 0.dp
            val reserved  = btnHeight + btnVPad
            val maxListHeight = maxHeight - reserved

            Column(Modifier.fillMaxSize()) {
                if (!hasError) {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxWidth()
                            // <<< ограничение высоты списка
                            .heightIn(max = maxListHeight),
                    ) {
                        items(
                            items = state.displayedTracks,
                            key = { it.trackId }
                        ) { track ->
                            TrackRow(
                                track = track,
                                onClick = { onItemClick(track) },
                                onRemove = { onRemoveClick(track) },
<<<<<<< Updated upstream
                                nameColorOverride          = rowTextColorOverride ?: state.listTextColor?.let { Color(it) },
                                arrowColorOverride         = rowIconColorOverride ?: state.listArrowColor?.let { Color(it) },
                                rowBackgroundColorOverride = rowBackgroundOverride ?: state.listItemBackgroundColor?.let { Color(it) }
=======
                                nameColorOverride = state.listTextColor,
                                arrowColorOverride = state.listArrowColor,
                                rowBackgroundColorOverride = state.listItemBackgroundColor
>>>>>>> Stashed changes
                            )
                        }
                    }
                } else {
                    // когда ошибка — просто занимаем доступную высоту под ошибку
                    Spacer(
                        Modifier
                            .fillMaxWidth()
                            .heightIn(max = maxListHeight)
                    )
                }
<<<<<<< Updated upstream
                if (showBottomButton) {
                    UpdateButton(
                        textRes = if (showClean) R.string.clean else R.string.update,
                        onClick = { if (showClean) onHistoryClear() else onRetry() },
                        modifier = Modifier.padding(top = btnVPad),
                        minHeight = btnHeight
                    )
=======
                // Кнопка снизу: "Очистить" / "Обновить" — стиль = SearchUpdate(Text14)
                if (showBottomButton) {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(top = btnVPad),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Button(
                            onClick = { if (showClean) onHistoryClear() else onRetry() },
                            shape = RoundedCornerShape(dimensionResource(R.dimen.track_45)),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colorResource(R.color.textColor_white)
                            ),
                            modifier = Modifier
                                .defaultMinSize(minHeight = btnHeight)
                        ) {
                            Text(
                                text = stringResource(if (showClean) R.string.clean else R.string.update),
                                color = colorResource(R.color.white_textColor),
                                fontSize = 14.sp, // Text14
                                fontFamily = FontFamily(Font(R.font.ys_display_medium)),
                                fontWeight = FontWeight.Medium // 500
                            )
                        }
                    }
>>>>>>> Stashed changes
                }
            }
        }
    }
}

// ────────────────────────────────────────────────────────────────────────────────
// Поле ввода: те же размеры/цвета, не «режет» текст, репортит фокус во VM.
// ────────────────────────────────────────────────────────────────────────────────

@Composable
private fun SearchInput(
    state: SearchUiState,
    onQueryChange: (String) -> Unit,
    onClear: () -> Unit,
<<<<<<< Updated upstream
    onFocusChanged: (Boolean) -> Unit,
    fieldBgOverride: Color? = null
=======
    onFocusChanged: (Boolean) -> Unit
>>>>>>> Stashed changes
) {
    val paddingH = dimensionResource(R.dimen.searchPaddingLeftRight_16)
    val paddingV = dimensionResource(R.dimen.searchPaddingTopBottom_8)
    val fieldMinHeight = dimensionResource(R.dimen.searchLineHeight_52)
<<<<<<< Updated upstream
    val fieldBg = fieldBgOverride ?: colorResource(R.color.hintField_white)
=======
    val fieldBg = colorResource(R.color.hintField_white)
>>>>>>> Stashed changes
    val hintColor = colorResource(R.color.hintColor_textColor)
    val cursorColor = colorResource(R.color.cursorColor)
    val ysDisplay = remember { FontFamily(Font(R.font.ys_display_regular)) }

    var text by remember(state.query) { mutableStateOf(state.query) }

    val interaction = remember { MutableInteractionSource() }
    LaunchedEffect(interaction) {
        interaction.interactions.collect { inter ->
            when (inter) {
                is FocusInteraction.Focus   -> onFocusChanged(true)
                is FocusInteraction.Unfocus -> onFocusChanged(false)
            }
        }
    }

    OutlinedTextField(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = paddingH, vertical = paddingV)
            .defaultMinSize(minHeight = fieldMinHeight),
        value = text,
        onValueChange = {
            text = it
            onQueryChange(it)
        },
        singleLine = true,
        textStyle = LocalTextStyle.current.copy(
            fontSize = dimensionResource(R.dimen.Settings_Text_16).value.sp,
            fontFamily = ysDisplay,
            fontWeight = FontWeight.W400,
            platformStyle = PlatformTextStyle(includeFontPadding = true)
        ),
        placeholder = {
            if (state.query.isBlank() && !state.isInputFocused) {
                Text(
                    text = stringResource(R.string.search_hint),
                    color = hintColor,
                    fontSize = dimensionResource(R.dimen.Settings_Text_16).value.sp,
                    fontFamily = ysDisplay,
                    fontWeight = FontWeight.W400
                )
            }
        },
        leadingIcon = {
            Icon(
                painter = painterResource(R.drawable.search_icon),
                contentDescription = null,
                tint = hintColor
            )
        },
        trailingIcon = {
            if (state.isClearIconVisible) {
                IconButton(onClick = {
                    onClear()
                    text = ""
                }) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = stringResource(R.string.clear_search),
                        tint = hintColor
                    )
                }
            }
        },
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color.Transparent,
            unfocusedBorderColor = Color.Transparent,
            focusedContainerColor = fieldBg,
            unfocusedContainerColor = fieldBg,
            cursorColor = cursorColor
        ),
        shape = MaterialTheme.shapes.small,
        interactionSource = interaction
    )
}
<<<<<<< Updated upstream
=======

// ────────────────────────────────────────────────────────────────────────────────
// Блок ошибки/фейла — визуально повторяет поведение AudioErrorManager.
// ────────────────────────────────────────────────────────────────────────────────

// option from resources FailBlock1
@Composable
private fun FailBlock1(@StringRes textId: Int, enabled: Boolean, topMargin: Dp = 50.dp) {
    AndroidView(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = topMargin), // внешний зазор сверху, если нужен
        factory = { ctx ->
            (LayoutInflater.from(ctx).inflate(R.layout.fail, null, false) as TextView).apply {
                // стиль SearchFail применится, НО мы поправим вертикальный сдвиг
                visibility = View.VISIBLE           // переопределяем GONE
                isEnabled = enabled                 // включает нужный item в fail_icon.xml
                setText(textId)

                // чтобы блок был сразу под инпутом
                setPadding(paddingLeft, 0, paddingRight, paddingBottom)
                // если используем RTL:
                // ViewCompat.setPaddingRelative(this, paddingStart, 0, paddingEnd, paddingBottom)

                // на всякий случай горизонтально по центру
                (layoutParams as? ViewGroup.MarginLayoutParams)?.let { lp ->
                    lp.width = ViewGroup.LayoutParams.WRAP_CONTENT
                    lp.height = ViewGroup.LayoutParams.WRAP_CONTENT
                }
                gravity = Gravity.CENTER_HORIZONTAL
            }
        },
        update = { tv ->
            tv.visibility = View.VISIBLE
            tv.isEnabled = enabled
            tv.setText(textId)
            // держим topPadding = 0 при обновлениях
            tv.setPadding(tv.paddingLeft, 0, tv.paddingRight, tv.paddingBottom)
        }
    )
}

// option local FailBlock
@Composable
private fun FailBlock(@StringRes textId: Int, enabled: Boolean) {
    val topPadding   = dimensionResource(R.dimen.track_45)
    val drawablePad  = dimensionResource(R.dimen.Padding_16)
    val textSizeSp   = dimensionResource(R.dimen.Search_Text_19).value.sp
    val textColor    = colorResource(R.color.textColor_white)
    val ysMedium     = remember { FontFamily(Font(R.font.ys_display_medium)) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AndroidView(
            // без .size(...) — позволяем wrap_content
            modifier = Modifier.padding(top = topPadding),
            factory = { ctx ->
                ImageView(ctx).apply {
                    setImageResource(R.drawable.fail_icon)
                    isEnabled = enabled
                    // критично:
                    adjustViewBounds = true
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                    scaleType = ImageView.ScaleType.CENTER_INSIDE
                }
            },
            update = { iv -> iv.isEnabled = enabled }
        )

        Spacer(Modifier.height(drawablePad))

        Text(
            text = stringResource(textId),
            color = textColor,
            fontSize = textSizeSp,
            fontFamily = ysMedium,
            fontWeight = FontWeight.W400,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
            style = LocalTextStyle.current.copy(
                platformStyle = PlatformTextStyle(includeFontPadding = true)
            )
        )
    }
}

// ────────────────────────────────────────────────────────────────────────────────
// Один элемент списка — повторяет XML (имена/цвета/радиус/доты/стрелка).
// ────────────────────────────────────────────────────────────────────────────────

@Composable
private fun TrackRow(
    track: Track,
    onClick: () -> Unit,
    onRemove: () -> Unit,
    nameColorOverride: Int? = null,
    arrowColorOverride: Int? = null,
    rowBackgroundColorOverride: Int? = null
) {
    val itemMinHeight = dimensionResource(R.dimen.Settings_Height_61)
    val paddingV = dimensionResource(R.dimen.Drawable_padding_8)
    val imageSize = dimensionResource(R.dimen.track_45)
    val imageStart = dimensionResource(R.dimen.track_13)
    val arrowSize = dimensionResource(R.dimen.arrow_back_24)
    val arrowEnd = dimensionResource(R.dimen.Icon_12)

    val nameColor = nameColorOverride?.let { Color(it) }
        ?: colorResource(R.color.black_white)

    val secondaryBase = colorResource(R.color.hintColor_white)
    // ← НОВОЕ: линия автора/точка/длительность красится тем же override, если он есть
    val authorLineColor = nameColorOverride?.let { Color(it) } ?: secondaryBase

    val arrowTint = arrowColorOverride?.let { Color(it) } ?: secondaryBase

    val rowBg = rowBackgroundColorOverride?.let { Color(it) } ?: Color.Transparent

    val ysDisplay = remember { FontFamily(Font(R.font.ys_display_regular)) }
    val titleSp = dimensionResource(R.dimen.Settings_Text_16).value.sp
    val authorSp = 11.sp

    val radiusPx = with(LocalDensity.current) { 2.dp.toPx() }.toInt()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(rowBg)
            .defaultMinSize(minHeight = itemMinHeight)
            .clickable(onClick = onClick)
            .padding(vertical = paddingV),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(Modifier.width(imageStart))

        AndroidView(
            modifier = Modifier
                .size(imageSize)
                .clip(RoundedCornerShape(2.dp)),
            factory = { ctx ->
                ImageView(ctx).apply {
                    scaleType = ImageView.ScaleType.CENTER_CROP
                    val glide = Glide.with(ctx)
                    val url = track.artworkUrlSmall
                    if (url.isNotBlank()) {
                        glide.load(url)
                            .transform(RoundedCorners(radiusPx))
                            .placeholder(R.drawable.placeholder)
                            .error(R.drawable.placeholder)
                            .into(this)
                    } else {
                        glide.load(R.drawable.placeholder)
                            .transform(RoundedCorners(radiusPx))
                            .into(this)
                    }
                }
            }
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = dimensionResource(R.dimen.Drawable_padding_8))
        ) {
            // track_name
            Text(
                text = track.trackName,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = nameColor,
                fontSize = titleSp,
                fontFamily = ysDisplay,
                fontWeight = FontWeight.W400
            )

            // ---------- горизонтальное ограничение автора остаётся как было ----------
            BoxWithConstraints(Modifier.fillMaxWidth()) {
                val textMeasurer = rememberTextMeasurer()
                val durationStyle = TextStyle(
                    fontSize = authorSp,
                    fontFamily = ysDisplay,
                    fontWeight = FontWeight.W400,
                    color = authorLineColor          // ← БЫЛО: secondaryColor
                )

                val durationText = track.trackDuration
                val durationWidthPx = textMeasurer
                    .measure(AnnotatedString(durationText), style = durationStyle)
                    .size.width
                val density = LocalDensity.current
                val durationWidth = with(density) { durationWidthPx.toDp() }

                val gapBeforeDot = 4.dp
                val dotSize = 12.dp
                val gapAfterDot = 4.dp

                val reservedRight = gapBeforeDot + dotSize + gapAfterDot + durationWidth
                val authorMaxWidth = (maxWidth - reservedRight).coerceAtLeast(0.dp)

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // track_author
                    Text(
                        text = track.artistName,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = authorLineColor,        // ← БЫЛО: secondaryColor
                        fontSize = authorSp,
                        fontFamily = ysDisplay,
                        fontWeight = FontWeight.W400,
                        modifier = Modifier.widthIn(max = authorMaxWidth)
                    )

                    Spacer(Modifier.width(gapBeforeDot))

                    // dot
                    Image(
                        painter = painterResource(R.drawable.dot),
                        contentDescription = null,
                        colorFilter = ColorFilter.tint(authorLineColor), // ← БЫЛО: secondaryColor
                        modifier = Modifier.size(dotSize)
                    )

                    Spacer(Modifier.width(gapAfterDot))

                    // track_duration
                    Text(
                        text = durationText,
                        style = durationStyle
                    )
                }
            }
            // -------------------------------------------------------------------------
        }

        Box(
            modifier = Modifier
                .padding(end = arrowEnd)
                .size(arrowSize)
        ) {
            IconButton(
                onClick = onRemove,
                modifier = Modifier.matchParentSize()
            ) {
                Icon(
                    painter = painterResource(R.drawable.vector),
                    contentDescription = null,
                    tint = arrowTint
                )
            }
        }
    }
}

// ────────────────────────────────────────────────────────────────────────────────
// опции для переиспользования
// ────────────────────────────────────────────────────────────────────────────────

@Composable
fun SearchHistoryHeader(
    visible: Boolean,
    text: String = stringResource(R.string.search_history)
) {
    if (!visible) return

    val h = dimensionResource(R.dimen.searchLineHeight_52)
    val marginTop = dimensionResource(R.dimen.arrow_back_24)
    val marginBottom = dimensionResource(R.dimen.searchPaddingTopBottom_8)
    val color = colorResource(R.color.black_white)

    // из Text19
    val textSizeSp = dimensionResource(R.dimen.Search_Text_19).value.sp
    val ysDisplayMedium = remember { FontFamily(Font(R.font.ys_display_medium)) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = marginTop, bottom = marginBottom)
            .height(h),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = color,
            fontSize = textSizeSp,
            fontFamily = ysDisplayMedium,
            fontWeight = FontWeight.Medium, // 500
            // в XML включена метрика шрифта; добавим аналог
            style = LocalTextStyle.current.copy(
                platformStyle = PlatformTextStyle(includeFontPadding = true)
            ),
            maxLines = 1
        )
    }
}

@Composable
private fun UpdateOrCleanButton( // for future re-use
    isClean: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    minHeight: Dp = dimensionResource(R.dimen.searchLineHeight_52) // соответствует твоему резерву
) {
    val corner    = dimensionResource(R.dimen.track_45)
    val textColor = colorResource(R.color.white_textColor)
    val container = colorResource(R.color.textColor_white)

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {
        Button(
            onClick = onClick,
            shape = RoundedCornerShape(corner),
            colors = ButtonDefaults.buttonColors(containerColor = container),
            modifier = Modifier.defaultMinSize(minHeight = minHeight)
        ) {
            Text(
                text = stringResource(if (isClean) R.string.clean else R.string.update),
                color = textColor,
                fontSize = 14.sp, // Text14
                fontFamily = FontFamily(Font(R.font.ys_display_medium)),
                fontWeight = FontWeight.Medium
            )
        }
    }
}

// use option:
//if (showBottomButton) {
//    UpdateOrCleanButton(
//        isClean = showClean,
//        onClick = { if (showClean) onHistoryClear() else onRetry() },
//        modifier = Modifier.padding(top = btnVPad),       // внешний отступ сверху
//        minHeight = btnHeight                             // тот же, что используешь в резерве
//    )
//}
>>>>>>> Stashed changes
