package com.example.playlistmaker.presentation.utils

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance

fun Color.lighten(amount: Float): Color {
    val a = amount.coerceIn(0f, 1f)
    return Color(
        red = red + (1f - red) * a,
        green = green + (1f - green) * a,
        blue = blue + (1f - blue) * a,
        alpha = alpha
    )
}

fun Color.darken(amount: Float): Color {
    val a = amount.coerceIn(0f, 1f)
    return Color(
        red = red * (1f - a),
        green = green * (1f - a),
        blue = blue * (1f - a),
        alpha = alpha
    )
}

/** На светлом фоне — чуть темнее, на тёмном — чуть светлее. */
fun deriveFieldBgFromScreen(bg: Color): Color {
    val lum = 0.299f * bg.red + 0.587f * bg.green + 0.114f * bg.blue
    return if (lum > 0.6f) bg.darken(0.6f) else bg.lighten(0.6f)
}

fun contrastOn(bg: Color, threshold: Float = 0.55f): Color =
    if (bg.luminance() > threshold) Color.Black else Color.White