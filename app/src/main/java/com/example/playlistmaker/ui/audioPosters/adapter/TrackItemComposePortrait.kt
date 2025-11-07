package com.example.playlistmaker.ui.audioPosters.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.Hyphens
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.zIndex
import androidx.constraintlayout.compose.ChainStyle
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.core.content.ContextCompat
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.DefaultTimeBar
import androidx.media3.ui.PlayerView
import androidx.media3.ui.TimeBar
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.placeholder
import com.example.playlistmaker.R
import com.example.playlistmaker.domain.models.search.Track

@Composable
fun TrackItemComposePortrait(
    context: Context,
    track: Track,
    isVideoAttached: Boolean,
    timebarVertical: Boolean,
    currentPlayer: ExoPlayer?,
    overlayPlayTime: String?,
    audioProgress: Any?,
    onPlayToggle: () -> Unit,
    onItemClick: () -> Unit,
    onBack: () -> Unit,
    onFav: () -> Unit,
    onAdd: () -> Unit,
    onSeek: (Long) -> Unit,
    style: TrackAdapterAudio.ItemStyle,
    textColorOverride: Color? = null,
    textColorOverrideAux: Color? = null,
    iconTintOverride:  Color? = null,
    iconTintOverrideAux:  Color? = null,
    backgroundOverride: Color? = null,
) {
    val defaultTextColor: Color = colorResource(R.color.textColor_white)
    val defaultTextColorAux: Color = colorResource(R.color.hintColor)
    val defaultIconTint:  Color = colorResource(R.color.textColor_white)
    val defaultIconTintAux:  Color = colorResource(R.color.hintColor)
    val defaultBackground: Color = colorResource(R.color.white_textColor)

    val textColor = textColorOverride ?: defaultTextColor
    val textColorAux = textColorOverrideAux ?: defaultTextColorAux
    val iconTint  = iconTintOverride  ?: defaultIconTint
    val iconTintAux  = iconTintOverrideAux  ?: defaultIconTintAux
    val bgColor   = backgroundOverride ?: defaultBackground

    val side24            = dimensionResource(R.dimen.arrow_back_24)
    val pad16             = dimensionResource(R.dimen.Padding_16)
    val authorMarginTop   = dimensionResource(R.dimen.author_margin_top)
    val authorMarginBot   = dimensionResource(R.dimen.author_margin_button)
    val durationMarginTop = dimensionResource(R.dimen.duration_margin_top)
    val albumMarginTop    = dimensionResource(R.dimen.album_margin_top)
    val bottom4           = dimensionResource(R.dimen.marginBottom_4)

    ConstraintLayout(
        modifier = Modifier
            .fillMaxSize()
            .clickable { onItemClick() }
    ) {
        val leftBorder    = createGuidelineFromStart(side24)
        val rightBorder   = createGuidelineFromEnd(side24)
        val gPercent = horizontalGuidelinePercent()
        val horizontalGL = createGuidelineFromTop(gPercent)
        val leftBorder16  = createGuidelineFromStart(pad16)
        val rightBorder16 = createGuidelineFromEnd(pad16)

        val (
            posterRef, nameRef, playTimeRef,
            durationRef, durationValRef,
            albumRef, albumValRef,
            yearRef, yearValRef,
            genreRef, genreValRef,
            countryRef,
            addRef, playRef, favRef,
            authorRef
        ) = createRefs()
        val countryValRef = createRef()

        var posterHeightPx by remember { mutableIntStateOf(0) }

        // ===== POSTER (FrameLayout 0dp x 0dp) =====
        Box(
            modifier = Modifier
                .constrainAs(posterRef) {
                    start.linkTo(leftBorder16)
                    end.linkTo(rightBorder16)
                    top.linkTo(parent.top)
                    bottom.linkTo(nameRef.top)
                    width = Dimension.fillToConstraints
                    height = Dimension.fillToConstraints
                }
                .clipToBounds() // дети не выходят за рамки постера
                .onGloballyPositioned { coords -> posterHeightPx = coords.size.height }
        ) {
            if (isVideoAttached && currentPlayer != null) {
                PlayerViewBox(
                    player = currentPlayer,
                    timebarVertical = timebarVertical,
                    barLengthPx = posterHeightPx
                )
            } else {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(track.artworkUrl512)
                        .placeholder(R.drawable.placeholder2)
                        .build(),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                AudioTimebarView(
                    vertical = timebarVertical,
                    progress = audioProgress as? TrackAdapterAudio.AudioProgress,
                    onSeek = onSeek,
                    containerHeightPx = posterHeightPx
                )
            }

            // Стрелка НУЖНА ТОЛЬКО для TRACK_ITEM_2
            if (style == TrackAdapterAudio.ItemStyle.TRACK_ITEM_2) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp)
                ) {
                    Icon(painterResource(R.drawable.arrow_back), contentDescription = "Back", tint = Color.Red)
                }
            }
        }

        // ===== track_name =====
        Text(
            text = track.trackName,
            color = textColor,
            style = LegacyTextStyles.text22_400().copy(
                platformStyle = androidx.compose.ui.text.PlatformTextStyle(includeFontPadding = false),
                lineBreak = LineBreak.Heading,   // или .Simple / .Paragraph (API 33+ эффективнее)
                hyphens = Hyphens.Auto           // API 33+
            ),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Start,
            modifier = Modifier.constrainAs(nameRef) {
                start.linkTo(leftBorder)
                end.linkTo(rightBorder)
                top.linkTo(posterRef.bottom)
                bottom.linkTo(horizontalGL)
                width  = Dimension.fillToConstraints           // зажать по гайдлайнам
                height = Dimension.wrapContent
                verticalBias = 1f
                horizontalBias = 0f
            }
        )

        // ===== play_time — якорит низ «резинки» =====
        Text(
            text = overlayPlayTime ?: (track.playTime ?: context.getString(R.string.set_time)),
            color = textColor,
            style = LegacyTextStyles.Text14.copy(color = colorResource(id = R.color.textColor_white)),
            modifier = Modifier.constrainAs(playTimeRef) {
                start.linkTo(leftBorder); end.linkTo(rightBorder)
                top.linkTo(playRef.bottom)
                bottom.linkTo(durationRef.top)
            }
        )

        // ===== ADD / PLAY / FAV =====
        val edgePad = 20.dp
        Column(
            modifier = Modifier.constrainAs(authorRef) {
                start.linkTo(leftBorder); end.linkTo(rightBorder)
                top.linkTo(horizontalGL)
                bottom.linkTo(playRef.top)
                width = Dimension.fillToConstraints
                height = Dimension.fillToConstraints
            }
        ) {
            Spacer(Modifier.height(authorMarginTop))
            Text(
                text = track.artistName,
                color = textColor,
                style = LegacyTextStyles.text14_400(),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Start
            )
            Spacer(Modifier.height(authorMarginBot))
        }

        IconButton(
            onClick = onAdd,
            modifier = Modifier
                .padding(start = edgePad)
                .constrainAs(addRef) {
                    start.linkTo(leftBorder)
                    end.linkTo(playRef.start)
                    top.linkTo(authorRef.bottom)
                    bottom.linkTo(playRef.bottom)
                    width = Dimension.wrapContent
                    height = Dimension.wrapContent
                }
        ) {
            // Фон-круг красим твоим tint (iconTintAux)
            Box(
                modifier = Modifier
                    .size(60.dp)                          // под размер старого вектора
                    .background(iconTintAux, CircleShape), // красится ТОЛЬКО фон
                contentAlignment = Alignment.Center
            ) {
                // Внутренний «плюс» всегда белый
                Icon(
                    painter = painterResource(R.drawable.ic_add_glyph),
                    contentDescription = "Add",
                    tint = Color.White,
                    modifier = Modifier.size(38.dp)
                )
            }
        }

        Surface(
            onClick = onPlayToggle,
            color = Color.Transparent,
            tonalElevation = 0.dp,
            shadowElevation = 0.dp,
            modifier = Modifier
                .heightIn(min = 65.dp)          // минимальная высота «резинки»
                .constrainAs(playRef) {
                    start.linkTo(addRef.end)
                    end.linkTo(favRef.start)
                    top.linkTo(authorRef.bottom)
                    bottom.linkTo(playTimeRef.top)
                    height = Dimension.fillToConstraints // «резинка» по вертикали
                    width = Dimension.wrapContent
                }
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(horizontal = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painterResource(if (track.isPlaying) R.drawable.pause else R.drawable.play),
                    contentDescription = "Play/Pause",
                    tint = iconTint
                )
            }
        }
        // Вариант без анимации
        IconButton(
            onClick = onFav,
            modifier = Modifier
                .padding(end = edgePad)
                .constrainAs(favRef) {
                    start.linkTo(playRef.end)
                    end.linkTo(rightBorder)
                    top.linkTo(authorRef.bottom)
                    bottom.linkTo(playRef.bottom)
                    width = Dimension.wrapContent
                    height = Dimension.wrapContent
                }
        ) {
            // Кольцо (фон) — красим только его
            Box(
                modifier = Modifier
                    .size(51.dp)                           // как у исходного вектора
                    .background(
                        color = iconTintAux,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                // Внутренний глиф — всегда красный, поэтому tint отключаем
                Icon(
                    painter = painterResource(if (track.isFavorite) R.drawable.favorite1 else R.drawable.favorite),
                    contentDescription = "Favorite",
                    tint = Color.Unspecified,              // не перекрашиваем глиф
                    modifier = Modifier.size(38.dp)        // подгони размер по вкусу
                )
            }
        }

        createHorizontalChain(addRef, playRef, favRef, chainStyle = ChainStyle.SpreadInside)

        // ===== детали =====
        Text(
            text = stringResourceCompat(context, R.string.duration),
            color = textColorAux,
            style = LegacyTextStyles.text13_400(),
            modifier = Modifier.constrainAs(durationRef) {
                start.linkTo(leftBorder16)
                top.linkTo(playTimeRef.bottom, margin = durationMarginTop)
            }
        )
        Text(
            text = track.trackDuration,
            color = textColorAux,
            style = LegacyTextStyles.text13_400(),
            modifier = Modifier.constrainAs(durationValRef) {
                end.linkTo(rightBorder16)
                top.linkTo(playTimeRef.bottom)
                baseline.linkTo(durationRef.baseline)
            }
        )

        Text(
            text = stringResourceCompat(context, R.string.album),
            color = textColorAux,
            style = LegacyTextStyles.text13_400(),
            modifier = Modifier.constrainAs(albumRef) {
                start.linkTo(leftBorder16)
                top.linkTo(durationRef.bottom, margin = albumMarginTop)
            }
        )
        Text(
            text = track.collectionName,
            color = textColorAux,
            style = LegacyTextStyles.text13_400(),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.End,
            modifier = Modifier.constrainAs(albumValRef) {
                start.linkTo(durationRef.end)
                end.linkTo(rightBorder16)
                baseline.linkTo(albumRef.baseline)
                width = Dimension.fillToConstraints
            }
        )

        Text(
            text = stringResourceCompat(context, R.string.year),
            color = textColorAux,
            style = LegacyTextStyles.text13_400(),
            modifier = Modifier.constrainAs(yearRef) {
                start.linkTo(leftBorder16)
                top.linkTo(albumValRef.bottom, margin = albumMarginTop)
            }
        )
        Text(
            text = track.releaseDate.takeWhile { it != '-' },
            color = textColorAux,
            style = LegacyTextStyles.text13_400(),
            modifier = Modifier.constrainAs(yearValRef) {
                end.linkTo(rightBorder16)
                top.linkTo(albumValRef.bottom, margin = albumMarginTop)
                baseline.linkTo(yearRef.baseline)
            }
        )

        Text(
            text = stringResourceCompat(context, R.string.genre),
            color = textColorAux,
            style = LegacyTextStyles.text13_400(),
            modifier = Modifier.constrainAs(genreRef) {
                start.linkTo(leftBorder16)
                top.linkTo(yearRef.bottom, margin = albumMarginTop)
            }
        )
        Text(
            text = track.primaryGenreName,
            color = textColorAux,
            style = LegacyTextStyles.text13_400(),
            modifier = Modifier.constrainAs(genreValRef) {
                end.linkTo(rightBorder16)
                top.linkTo(yearRef.bottom, margin = albumMarginTop)
                baseline.linkTo(genreRef.baseline)
            }
        )

        Text(
            text = stringResourceCompat(context, R.string.country),
            color = textColorAux,
            style = LegacyTextStyles.text13_400(),
            modifier = Modifier.constrainAs(countryRef) {
                start.linkTo(leftBorder16)
                top.linkTo(genreRef.bottom, margin = albumMarginTop)
                bottom.linkTo(parent.bottom, margin = bottom4)
            }
        )
        Text(
            text = track.country,
            color = textColorAux,
            style = LegacyTextStyles.text13_400(),
            modifier = Modifier.constrainAs(countryValRef) {
                end.linkTo(rightBorder16)
                top.linkTo(genreValRef.bottom)
                baseline.linkTo(countryRef.baseline)
                bottom.linkTo(parent.bottom, margin = bottom4)
            }
        )

        createTopBarrier(
            durationRef, durationValRef,
            albumRef, albumValRef, yearRef, yearValRef,
            genreRef, genreValRef, countryRef, countryValRef
        )
    }
}

// ---------- PlayerView с контроллером и «вертикальным» таймбаром внутри ----------
@SuppressLint("InflateParams")
@OptIn(UnstableApi::class)
@Composable
fun PlayerViewBox(
    player: ExoPlayer,
    timebarVertical: Boolean,
    barLengthPx: Int
) {
    if (LocalInspectionMode.current) {
        Box(Modifier.fillMaxSize())
        return
    }

    var pv: PlayerView? = null

    AndroidView(
        modifier = Modifier
            .fillMaxSize()
            .clipToBounds(), // сам контейнер уже обрезает, но продублируем
        factory = { ctx ->
            val view = LayoutInflater.from(ctx)
                .inflate(R.layout.player_view_with_controller, null, false) as PlayerView
            view.useController = true
            view.controllerShowTimeoutMs = 0
            // как ImageView centerCrop / ContentScale.Crop — не выходим за рамки постера
            view.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
            view.player = player

            // внутри PlayerView тоже не даём детям вылезать
            view.clipToPadding = true
            view.clipChildren = true

            pv = view
            view
        },
        update = { v ->
            v.player = player
            v.showController()
            pv = v
        }
    )

    // настраиваем полоску прогресса внутри PlayerView
    DisposableEffect(timebarVertical, barLengthPx, player) {
        val view = pv
        if (view != null) {
            view.post {
                val tb = view.findViewById<DefaultTimeBar?>(androidx.media3.ui.R.id.exo_progress) ?: return@post

                (tb.parent as? ViewGroup)?.apply {
                    clipToPadding = true
                    clipChildren = true
                }

                val thicknessPx = dp(view.context, 12)
                val lp = (tb.layoutParams as? FrameLayout.LayoutParams)
                    ?: FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        thicknessPx
                    )

                if (timebarVertical) {
                    tb.rotation = 270f
                    tb.pivotX = 0f
                    tb.pivotY = 0f
                    val maxLen = view.height.takeIf { it > 0 } ?: barLengthPx
                    val length = minOf(maxLen, if (barLengthPx > 0) barLengthPx else dp(view.context, 200))
                    lp.width  = length
                    lp.height = thicknessPx
                    lp.gravity = Gravity.BOTTOM or Gravity.START
                } else {
                    tb.rotation = 0f
                    tb.pivotX = 0f
                    tb.pivotY = 0f
                    lp.width  = FrameLayout.LayoutParams.MATCH_PARENT
                    lp.height = thicknessPx
                    lp.gravity = Gravity.BOTTOM
                }

                tb.translationX = 0f
                tb.translationY = 0f
                tb.layoutParams = lp

                // фирменные цвета
                try {
                    tb.setPlayedColor(ContextCompat.getColor(view.context, R.color.timebar_played))
                    tb.setScrubberColor(ContextCompat.getColor(view.context, R.color.timebar_scrubber))
                    tb.setBufferedColor(ContextCompat.getColor(view.context, R.color.timebar_buffered))
                    tb.setUnplayedColor(ContextCompat.getColor(view.context, R.color.timebar_unplayed))
                } catch (_: Throwable) {}
            }
        }
        onDispose { /* nothing */ }
    }
}

// ---------- Таймбар поверх картинки (аудио) ----------
@SuppressLint("InflateParams")
@OptIn(UnstableApi::class)
@Composable
fun BoxScope.AudioTimebarView(
    vertical: Boolean,
    progress: TrackAdapterAudio.AudioProgress?,
    onSeek: (Long) -> Unit,
    containerHeightPx: Int
) {
    val onSeekState = rememberUpdatedState(onSeek)
    val density = LocalDensity.current
    val barLengthDp = with(density) { (if (containerHeightPx > 0) containerHeightPx else 200).toDp() }
    val thicknessDp = 12.dp

    // аккуратный зазор как у видео
    val insetDp = 12.dp

    AndroidView(
        factory = { ctx ->
            (LayoutInflater.from(ctx).inflate(
                R.layout.exo_controller_timebar_only, null, false
            ) as DefaultTimeBar).apply {
                setPlayedColor   (ContextCompat.getColor(ctx, R.color.timebar_played))
                setScrubberColor (ContextCompat.getColor(ctx, R.color.timebar_scrubber))
                setBufferedColor (ContextCompat.getColor(ctx, R.color.timebar_buffered))
                setUnplayedColor (ContextCompat.getColor(ctx, R.color.timebar_unplayed))
                alpha = 1f
                translationZ = 10f

                val listener = object : TimeBar.OnScrubListener {
                    override fun onScrubStart(t: TimeBar, p: Long) {}
                    override fun onScrubMove(t: TimeBar, p: Long) {}
                    override fun onScrubStop(t: TimeBar, p: Long, canceled: Boolean) {
                        if (!canceled) onSeekState.value(p)
                    }
                }
                addListener(listener)
                tag = listener
            }
        },
        update = { tb ->
            if (progress == null) {
                tb.setDuration(0); tb.setBufferedPosition(0); tb.setPosition(0)
            } else {
                tb.setDuration(progress.durationMs.coerceAtLeast(0))
                tb.setBufferedPosition(progress.bufferedMs.coerceAtLeast(0))
                tb.setPosition(progress.positionMs.coerceAtLeast(0))
            }

            tb.rotation = 0f
            tb.post {
                val thicknessPx = dp(tb.context, 12)
                val lp = (tb.layoutParams as? ViewGroup.MarginLayoutParams)
                    ?: ViewGroup.MarginLayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        thicknessPx
                    )
                lp.width  = ViewGroup.LayoutParams.MATCH_PARENT
                lp.height = thicknessPx
                tb.layoutParams = lp
            }
        },
        modifier = if (vertical) {
            Modifier
                .align(Alignment.BottomStart)
                // даём микро-отступ слева
                .padding(start = insetDp)
                // и компенсируем длину, чтобы правый конец не «уехал»
                .width((barLengthDp - insetDp).coerceAtLeast(0.dp))
                .height(thicknessDp)
                // поворачиваем полосу
                .graphicsLayer {
                    rotationZ = 270f
                    transformOrigin = TransformOrigin(0f, 1f)
                }
                .zIndex(2f)
        } else {
            Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 6.dp)
                .fillMaxWidth()
                .height(thicknessDp)
                .zIndex(2f)
        }
    )
}

private fun dp(ctx: Context, v: Int): Int =
    (v * ctx.resources.displayMetrics.density + 0.5f).toInt()

// ---------- fraction -> percent для horizontalGL ----------
@SuppressLint("LocalContextResourcesRead")
@Composable
private fun horizontalGuidelinePercent(): Float {
    val ctx = LocalContext.current
    return ctx.resources.getFraction(R.fraction.guideline_percent, 1, 1)
}

// ---------- Утил ----------
@Composable
fun stringResourceCompat(context: Context, id: Int): String =
    remember(id) { context.getString(id) }

