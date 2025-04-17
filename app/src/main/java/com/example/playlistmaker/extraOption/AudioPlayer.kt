package com.example.playlistmaker.extraOption

import android.icu.text.SimpleDateFormat
import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import java.util.Locale

class AudioPlayer {

    private var mediaPlayer: MediaPlayer? = null
    private val handler = Handler(Looper.getMainLooper())
    private var onTimeUpdateCallback: ((String) -> Unit)? = null
    private val updateRunnable = object : Runnable {
        override fun run() {
            mediaPlayer?.let {
                val currentPos = it.currentPosition
                val time = SimpleDateFormat("mm:ss", Locale.getDefault()).format(currentPos)
                onTimeUpdateCallback?.invoke(time)
                handler.postDelayed(this, 300)
            }
        }
    }

    fun play(url: String, onTimeUpdate: (String) -> Unit, onComplete: () -> Unit) {
        stop()
        onTimeUpdateCallback = onTimeUpdate

        mediaPlayer = MediaPlayer().apply {
            setDataSource(url)
            prepare()
            start()
            setOnCompletionListener {
                onComplete()
                stop()
            }
        }

        handler.post(updateRunnable)
    }

    fun pause() {
        mediaPlayer?.pause()
        handler.removeCallbacks(updateRunnable)
    }

    fun resume() {
        mediaPlayer?.start()
        handler.post(updateRunnable)
    }

    fun stop() {
        handler.removeCallbacks(updateRunnable)
        mediaPlayer?.release()
        mediaPlayer = null
        onTimeUpdateCallback = null
    }

    fun isPlaying(): Boolean = mediaPlayer?.isPlaying == true
}
