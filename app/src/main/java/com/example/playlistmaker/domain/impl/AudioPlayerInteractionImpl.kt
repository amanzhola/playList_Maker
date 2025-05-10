package com.example.playlistmaker.domain.impl

import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import com.example.playlistmaker.domain.api.AudioPlayerInteraction
import java.util.Locale

class AudioPlayerInteractionImpl : AudioPlayerInteraction {

    private var mediaPlayer: MediaPlayer? = null
    private val handler = Handler(Looper.getMainLooper()) // 🚑
    private var onTimeUpdateCallback: ((String) -> Unit)? = null
    private var onStateChangeCallback: ((AudioPlayerInteraction.PlaybackState) -> Unit)? = null

    override var currentTrackId: Int = -1
        private set
    override var playbackState: AudioPlayerInteraction.PlaybackState = AudioPlayerInteraction.PlaybackState.IDLE
        private set

    private val updateRunnable = object : Runnable {
        override fun run() {
            mediaPlayer?.let { player -> // 📖 🎶
                if (player.isPlaying) {
                    val currentPos = player.currentPosition
                    onTimeUpdateCallback?.invoke(getFormattedTime(currentPos))
                    handler.postDelayed(this, 1000)
                }
            }
        }
    }

    companion object { // 💯 🏗
        @Volatile
        private var instance: AudioPlayerInteraction? = null

        fun getInstance(): AudioPlayerInteraction {
            return instance ?: synchronized(this) {
                instance ?: AudioPlayerInteractionImpl().also { instance = it }
            }
        }
    }

    override fun setOnTimeUpdateCallback(callback: (String) -> Unit) { // 🔁 🧩
        this.onTimeUpdateCallback = callback
    }

    override fun setStateChangeCallback(callback: (AudioPlayerInteraction.PlaybackState) -> Unit) { // 🔁 🧩
        this.onStateChangeCallback = callback
    }

    override fun setTrack(previewUrl: String, trackId: Int) { // 🎵 ✅ ✨🔄
        stop()
        currentTrackId = trackId
        playbackState = AudioPlayerInteraction.PlaybackState.PREPARING
        onStateChangeCallback?.invoke(playbackState)

        mediaPlayer = MediaPlayer().apply { // 💡 ⏭️
            setDataSource(previewUrl)
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .build()
            )
            setOnPreparedListener {// 📌
                playbackState = AudioPlayerInteraction.PlaybackState.PREPARED
                onStateChangeCallback?.invoke(playbackState)
                start() // ▶️ 💃 ⏭️
                playbackState = AudioPlayerInteraction.PlaybackState.PLAYING
                onStateChangeCallback?.invoke(playbackState)
                handler.post(updateRunnable)
            }
            setOnCompletionListener {// ⚠️ 📦
                onStateChangeCallback?.invoke(AudioPlayerInteraction.PlaybackState.STOPPED)
                onTimeUpdateCallback?.invoke("00:00")
                stop()
            }
            prepareAsync()
        }
        // отправляем статус PREPARING
        onStateChangeCallback?.invoke(AudioPlayerInteraction.PlaybackState.PREPARING)
    }

    override fun pause() { // ⏸️
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.pause()
                playbackState = AudioPlayerInteraction.PlaybackState.PAUSED
                onStateChangeCallback?.invoke(playbackState)
                handler.removeCallbacks(updateRunnable)
            }
        }
    }

    override fun resume() { // ⏹️ ▶️ + 🛑 00:00
        mediaPlayer?.let {
            if (!it.isPlaying && playbackState == AudioPlayerInteraction.PlaybackState.PAUSED) {
                it.start()
                playbackState = AudioPlayerInteraction.PlaybackState.PLAYING
                onStateChangeCallback?.invoke(playbackState)
                handler.post(updateRunnable)
            }
        }
    }

    override fun stop() { // 📛
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.stop()
                onStateChangeCallback?.invoke(AudioPlayerInteraction.PlaybackState.STOPPED)
                handler.removeCallbacks(updateRunnable)
            }
            release()
        }
        mediaPlayer = null
        currentTrackId = -1
        playbackState = AudioPlayerInteraction.PlaybackState.IDLE
    }

    private fun release() { // 🧹
        mediaPlayer?.release()
        mediaPlayer = null
    }

    override fun isCurrentTrackPlaying(trackId: Int): Boolean { // ⛷️
        return mediaPlayer?.isPlaying == true && currentTrackId == trackId // ☕
    }

    override fun getValidTrackId(): Int { // 🔥 100%
        return if (currentTrackId != -1) currentTrackId else -1
    }

    override fun clearCallbacks() { //  🤘
        onTimeUpdateCallback = null
        onStateChangeCallback = null
        handler.removeCallbacks(updateRunnable)
    }

    private fun getFormattedTime(milliseconds: Int): String { // 🌼
        val totalSeconds = milliseconds / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
    }

}