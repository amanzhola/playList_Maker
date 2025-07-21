package com.example.playlistmaker.utils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ClickDebouncer(
    private val delayMillis: Long,
    private val coroutineScope: CoroutineScope
) {
    private var isClickable = true

    fun tryClick(action: () -> Unit) {
        if (isClickable) {
            isClickable = false
            action()
            coroutineScope.launch {
                delay(delayMillis)
                isClickable = true
            }
        }
    }
}
