package com.example.playlistmaker.ui.audioPosters.adapter

import android.content.Context
import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.placeholder
import com.example.playlistmaker.R
import com.example.playlistmaker.domain.models.search.Track

@OptIn(UnstableApi::class)
@Composable
fun TrackItemComposeLand(
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
    fullScreen: Boolean = false,
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

    val pad16             = dimensionResource(R.dimen.Padding_16)
    val authorMarginTop   = dimensionResource(R.dimen.author_margin_top)
    val authorMarginBot   = dimensionResource(R.dimen.author_margin_button)
    val durationMarginTop = dimensionResource(R.dimen.duration_margin_top)
    val albumMarginTop    = dimensionResource(R.dimen.album_margin_top)
    val bottom4           = dimensionResource(R.dimen.marginBottom_4)

    // === FULLSCREEN ВЕТКА: только PlayerView на весь экран ===
    if (fullScreen && isVideoAttached && currentPlayer != null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(colorResource(R.color.textColor)) // фон для видео
        ) {
            // Создаём PlayerView ОДИН раз и настраиваем «анти-мигание»
            val shutterColor = androidx.core.content.ContextCompat.getColor(context, R.color.textColor)
            val pv = remember(context) {
                PlayerView(context).apply {
                    setUseController(true) // Media3 API
                    setKeepContentOnPlayerReset(true) // держать последний кадр при reset/ребуфере
                    setShutterBackgroundColor(shutterColor) // фон до первого кадра
                    setShowBuffering(PlayerView.SHOW_BUFFERING_WHEN_PLAYING)
                    setBackgroundColor(shutterColor) // на случай прозрачностей
                }
            }

            // Привязка/отвязка player без пересоздания PlayerView
            DisposableEffect(currentPlayer) {
                pv.player = currentPlayer
                onDispose { if (pv.player === currentPlayer) pv.player = null }
            }

            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { pv },
                update   = { it.player = currentPlayer }
            )

            // «Назад» поверх (тулбар скрыт)
            IconButton(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(12.dp),
                onClick = onBack
            ) {
                Icon(
                    painter = painterResource(R.drawable.arrow_back),
                    contentDescription = "arrow_back",
                    tint = Color.White
                )
            }
        }
        return
    }

    // === ОБЫЧНЫЙ РЕЖИМ (верстка, как было) ===
    ConstraintLayout(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
        // .clickable { onItemClick() }   // ❌ РАНЬШЕ: клик по всему экрану, включая постер
    ) {
        val (posterRef, detailsRef) = createRefs()

        // СЛЕВА: квадратный постер 1:1, растянутый по высоте
        var posterHeightPx by remember { mutableIntStateOf(0) }
        Box(
            modifier = Modifier
                .constrainAs(posterRef) {
                    start.linkTo(parent.start)
                    end.linkTo(detailsRef.start)
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                    width = Dimension.ratio("1:1")        // квадрат по ширине
                    height = Dimension.fillToConstraints  // растяжение по высоте
                }
                .clipToBounds()
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
                    Icon(
                        painter = painterResource(R.drawable.arrow_back),
                        contentDescription = "Back",
                        tint = Color.Red
                    )
                }
            }
        }

        // СПРАВА: прокручиваемые детали (аналог NestedScrollView)
        val scroll = rememberScrollState()
        Column(
            modifier = Modifier
                .constrainAs(detailsRef) {
                    start.linkTo(posterRef.end)
                    end.linkTo(parent.end)
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                    width = Dimension.fillToConstraints
                    height = Dimension.fillToConstraints
                }
                .padding(horizontal = pad16)
                .verticalScroll(scroll)
                .clickable { onItemClick() }  // NEW: клик ТОЛЬКО по правой части (детали) переключает гориз/верт
        ) {
            // Имя трека
            Text(
                text = track.trackName,
                color = textColor,
                style = LegacyTextStyles.text22_400().copy(
                    platformStyle = androidx.compose.ui.text.PlatformTextStyle(includeFontPadding = false)
                ),
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )

            // Автор
            Spacer(Modifier.height(authorMarginTop))
            Text(
                text = track.artistName,
                color = textColor,
                style = LegacyTextStyles.text14_400(),
                maxLines = 4,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Start
            )

            // ───────── РЯД: Add — Play — Fav (Play строго по центру) ─────────
            Spacer(Modifier.height(authorMarginBot))
            RowCenterControls(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = pad16)
            ) {
                // Левая треть — Add
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.CenterStart
                ) {
                    IconButton(onClick = onAdd) {
                        // ← вместо старого Icon(...)
                        Box(
                            modifier = Modifier
                                .size(51.dp)                            // как у старого вектора
                                .background(iconTintAux, CircleShape),  // красим ТОЛЬКО фон
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_add_glyph), // вектор только с «плюсом»
                                contentDescription = "Add",
                                tint = Color.White,                     // плюс всегда белый
                                modifier = Modifier.size(38.dp)
                            )
                        }
                    }
                }

                // Центр — Play
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    IconButton(onClick = onPlayToggle) {
                        Icon(
                            painter = painterResource(if (track.isPlaying) R.drawable.pause else R.drawable.play),
                            contentDescription = "Play/Pause",
                            tint = iconTint
                        )
                    }
                }

                // Правая треть — Fav
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    IconButton(onClick = onFav) {
                        Box(
                            modifier = Modifier
                                .size(51.dp)
                                .background(iconTintAux, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            // внутри — только глиф сердца, ВСЕГДА красный
                            Icon(
                                painter = painterResource(if (track.isFavorite) R.drawable.favorite1 else R.drawable.favorite),
                                contentDescription = "Favorite",
                                tint = Color.Unspecified,
                                modifier = Modifier.size(38.dp) // подгон размер под дизайн
                            )
                        }
                    }
                }

            }
            // ───────── конец ряда управления ─────────

            // Текущее время
            Spacer(Modifier.height(authorMarginTop))
            Text(
                text = overlayPlayTime ?: (track.playTime ?: context.getString(R.string.set_time)),
                color = textColor,
                style = LegacyTextStyles.Text14.copy(color = colorResource(id = R.color.textColor_white)),
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            // Длительность
            Spacer(Modifier.height(durationMarginTop))
            RowSpread(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    stringResourceCompat(context, R.string.duration),
                    color = textColorAux,
                    style = LegacyTextStyles.text13_400()
                )
                Text(
                    track.trackDuration,
                    color = textColorAux,
                    style = LegacyTextStyles.text13_400()
                )
            }

            // Альбом
            Spacer(Modifier.height(albumMarginTop))
            RowSpread(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    stringResourceCompat(context, R.string.album),
                    color = textColorAux,
                    style = LegacyTextStyles.text13_400()
                )
                Text(
                    track.collectionName,
                    color = textColorAux,
                    style = LegacyTextStyles.text13_400(),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.End,
                    modifier = Modifier.fillMaxWidth(0.7f) // чтобы длинное имя красиво ужималось
                )
            }

            // Год
            Spacer(Modifier.height(albumMarginTop))
            RowSpread(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    stringResourceCompat(context, R.string.year),
                    color = textColorAux,
                    style = LegacyTextStyles.text13_400()
                )
                Text(
                    track.releaseDate.takeWhile { it != '-' },
                    color = textColorAux,
                    style = LegacyTextStyles.text13_400()
                )
            }

            // Жанр
            Spacer(Modifier.height(albumMarginTop))
            RowSpread(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    stringResourceCompat(context, R.string.genre),
                    color = textColorAux,
                    style = LegacyTextStyles.text13_400()
                )
                Text(
                    track.primaryGenreName,
                    color = textColorAux,
                    style = LegacyTextStyles.text13_400()
                )
            }

            // Страна
            Spacer(Modifier.height(albumMarginTop))
            RowSpread(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    stringResourceCompat(context, R.string.country),
                    color = textColorAux,
                    style = LegacyTextStyles.text13_400()
                )
                Text(
                    track.country,
                    color = textColorAux,
                    style = LegacyTextStyles.text13_400()
                )
            }

            Spacer(Modifier.height(bottom4))
        }
    }
}
