package com.example.playlistmaker.ui.audioPosters.sheet

import android.annotation.SuppressLint
import android.widget.ImageView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.playlistmaker.R
import com.example.playlistmaker.domain.models.playlist.Playlist
import com.example.playlistmaker.presentation.ImageLoader
import com.example.playlistmaker.utils.parseCoverUri

@SuppressLint("UseKtx")
@Composable
fun PlaylistRow(
    playlist: Playlist,
    imageLoader: ImageLoader,
    onClick: () -> Unit,
    titleColor: androidx.compose.ui.graphics.Color,
    hintColor: androidx.compose.ui.graphics.Color,
    rowHeight: Dp,
    rowPadding: Dp,
    coverSize: Dp,
    between: Dp,
) {
    val rowBg = colorResource(R.color.white_textColor)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = rowHeight)
            .background(rowBg)
            .padding(all = rowPadding)        // @dimen/track_13
            .noRippleClickable(onClick),      // лёгкий helper
        verticalAlignment = Alignment.CenterVertically
    ) {
        // cover: используем ImageLoader через AndroidView(ImageView)
        AndroidView(
            factory = { ctx ->
                ImageView(ctx).apply {
                    scaleType = ImageView.ScaleType.CENTER_CROP
                }
            },
            modifier = Modifier
                .size(coverSize)              // @dimen/track_45
        ) { iv ->
            val uri = parseCoverUri(playlist.coverPath)
            imageLoader.load(iv, uri, R.drawable.placeholder)
        }

        Spacer(Modifier.width(between))        // @dimen/Drawable_padding_8

        Column(
            modifier = Modifier
                .weight(1f)
        ) {
            // tvName
            Text(
                text = playlist.name,
                color = titleColor,
                maxLines = 1,
                // 16sp, regular, 400 — как в XML
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = 16.sp,
                    fontFamily = androidx.compose.ui.text.font.FontFamily(
                        androidx.compose.ui.text.font.Font(R.font.ys_display_regular)
                    ),
                    fontWeight = androidx.compose.ui.text.font.FontWeight.W400
                )
            )

            Spacer(Modifier.height(1.dp))

            Text(
                // корректные множественные как было в адаптере
                text = pluralStringResource(R.plurals.tracks_count, playlist.tracksCount, playlist.tracksCount),
                color = hintColor,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 11.sp,
                    lineHeight = 13.sp,
                    fontFamily = androidx.compose.ui.text.font.FontFamily(
                        androidx.compose.ui.text.font.Font(R.font.ys_display_regular)
                    ),
                    fontWeight = androidx.compose.ui.text.font.FontWeight.W400,
                    platformStyle = PlatformTextStyle(includeFontPadding = true)
                )
            )
        }
    }
}
