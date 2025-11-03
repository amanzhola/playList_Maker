package com.example.playlistmaker.ui.audioPosters.adapter

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import com.example.playlistmaker.R

// ───────────────────────────────────────────────────────────────
// Универсальные обёртки (как были)
// ───────────────────────────────────────────────────────────────
@Composable
fun RowCenterControls(
    modifier: Modifier = Modifier,
    verticalAlignment: Alignment.Vertical = Alignment.CenterVertically,
    content: @Composable RowScope.() -> Unit
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = verticalAlignment,
        content = content
    )
}

@Composable
fun RowSpread(
    modifier: Modifier = Modifier,
    verticalAlignment: Alignment.Vertical = Alignment.CenterVertically,
    content: @Composable RowScope.() -> Unit
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = verticalAlignment,
        content = content
    )
}

// ───────────────────────────────────────────────────────────────
// Перегруженные «удобные» версии под текущие вызовы в TrackItemComposeLand
// ───────────────────────────────────────────────────────────────

/**
 * Центрированный ряд с тремя кнопками: Add | Play/Pause | Favorite.
 * Поведение соответствует вызовам в TrackItemComposeLand.
 */
@Composable
fun RowCenterControls(
    isPlaying: Boolean,
    onAdd: () -> Unit,
    onPlayToggle: () -> Unit,
    onFav: () -> Unit,
    isFavorite: Boolean,
    modifier: Modifier = Modifier,
    verticalAlignment: Alignment.Vertical = Alignment.CenterVertically,
) {
    RowCenterControls(
        modifier = modifier,
        verticalAlignment = verticalAlignment
    ) {
        IconButton(onClick = onAdd) {
            Icon(
                painter = painterResource(R.drawable.add_queue),
                contentDescription = "Add",
                tint = Color.Unspecified
            )
        }
        IconButton(onClick = onPlayToggle) {
            Icon(
                painter = painterResource(if (isPlaying) R.drawable.pause else R.drawable.play),
                contentDescription = "Play/Pause",
                tint = Color.Unspecified
            )
        }
        IconButton(onClick = onFav) {
            Icon(
                painter = painterResource(if (isFavorite) R.drawable.favorite1 else R.drawable.favorite),
                contentDescription = "Favorite",
                tint = Color.Unspecified
            )
        }
    }
}

/**
 * «Лево — право» со SpaceBetween: передай два слота.
 * Совместимо с вызовами RowSpread(left = { ... }, right = { ... }).
 */
@Composable
fun RowSpread(
    left: @Composable () -> Unit,
    right: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    verticalAlignment: Alignment.Vertical = Alignment.CenterVertically,
) {
    RowSpread(
        modifier = modifier,
        verticalAlignment = verticalAlignment
    ) {
        left()
        right()
    }
}
