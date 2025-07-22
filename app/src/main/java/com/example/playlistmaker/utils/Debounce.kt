package com.example.playlistmaker.utils

import android.os.Handler
import android.os.Looper

class Debounce(private val delayMillis: Long, private val intervalMillis: Long = 1000L) {
    private val handler = Handler(Looper.getMainLooper())
    private var runnable: Runnable? = null
    private var lastExecutionTime = 0L

    fun debounce(action: () -> Unit) {
        val currentTime = System.currentTimeMillis()

        if (currentTime - lastExecutionTime < intervalMillis) {
            return
        }

        runnable?.let { handler.removeCallbacks(it) }

        runnable = Runnable {
            action()
            lastExecutionTime = System.currentTimeMillis()
        }

        handler.postDelayed(runnable!!, delayMillis)
    }

    fun cancel() {
        runnable?.let { handler.removeCallbacks(it) }
    }
}
