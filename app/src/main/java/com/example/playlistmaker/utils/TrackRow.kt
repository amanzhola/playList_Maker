package com.example.playlistmaker.utils

import android.widget.ImageView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.playlistmaker.R
import com.example.playlistmaker.domain.models.search.Track

@Composable
fun TrackRow(
    track: Track,
    onClick: () -> Unit,
    onRemove: () -> Unit,
    nameColorOverride: Color? = null,
    arrowColorOverride: Color? = null,
    rowBackgroundColorOverride: Color? = null,
    onLongClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    // Жесты: tap + long-press
    val clickMod = if (onLongClick != null) {
        Modifier.pointerInput(onLongClick) {
            detectTapGestures(
                onTap = { onClick() },
                onLongPress = { onLongClick() }
            )
        }
    } else {
        Modifier.pointerInput(Unit) {
            detectTapGestures(onTap = { onClick() })
        }
    }

    val itemMinHeight = dimensionResource(R.dimen.Settings_Height_61)
    val paddingV = dimensionResource(R.dimen.Drawable_padding_8)
    val imageSize = dimensionResource(R.dimen.track_45)
    val imageStart = dimensionResource(R.dimen.track_13)
    val arrowSize = dimensionResource(R.dimen.arrow_back_24)
    val arrowEnd = dimensionResource(R.dimen.Icon_12)

    val nameColor = nameColorOverride ?: colorResource(R.color.black_white)
    val secondaryBase = colorResource(R.color.hintColor_white)
    val authorLineColor = nameColorOverride ?: secondaryBase
    val arrowTint = arrowColorOverride ?: secondaryBase
    val rowBg = rowBackgroundColorOverride ?: Color.Transparent

    val ysDisplay = remember { FontFamily(Font(R.font.ys_display_regular)) }
    val titleSp = dimensionResource(R.dimen.Settings_Text_16).value.sp
    val authorSp = 11.sp

    val radiusPx = with(LocalDensity.current) { 2.dp.toPx() }.toInt()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(rowBg)
            .defaultMinSize(minHeight = itemMinHeight)
            .padding(vertical = paddingV),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(Modifier.width(imageStart))

        // Левая кликабельная область (обложка + тексты) — сюда вешаем tap/long-press
        Row(
            modifier = Modifier
                .weight(1f)
                .then(clickMod),   // ← ВАЖНО: реально применяем жесты
            verticalAlignment = Alignment.CenterVertically
        ) {
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
                Text(
                    text = track.trackName,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = nameColor,
                    fontSize = titleSp,
                    fontFamily = ysDisplay,
                    fontWeight = FontWeight.W400
                )

                BoxWithConstraints(Modifier.fillMaxWidth()) {
                    val textMeasurer = rememberTextMeasurer()
                    val durationStyle = TextStyle(
                        fontSize = authorSp,
                        fontFamily = ysDisplay,
                        fontWeight = FontWeight.W400,
                        color = authorLineColor
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
                        Text(
                            text = track.artistName,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = authorLineColor,
                            fontSize = authorSp,
                            fontFamily = ysDisplay,
                            fontWeight = FontWeight.W400,
                            modifier = Modifier.widthIn(max = authorMaxWidth)
                        )

                        Spacer(Modifier.width(gapBeforeDot))

                        Image(
                            painter = painterResource(R.drawable.dot),
                            contentDescription = null,
                            colorFilter = ColorFilter.tint(authorLineColor),
                            modifier = Modifier.size(dotSize)
                        )

                        Spacer(Modifier.width(gapAfterDot))

                        Text(text = durationText, style = durationStyle)
                    }
                }
            }
        }

        // Хвостовая кнопка (удалить/стрелка)
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
