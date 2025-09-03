package com.example.playlistmaker.utils

/** Форматирует миллисекунды в "m:ss". */
fun formatDuration(ms: Long): String {
    val totalSec = (ms / 1000).toInt()
    val m = totalSec / 60
    val s = totalSec % 60
    return "%d:%02d".format(m, s)
}