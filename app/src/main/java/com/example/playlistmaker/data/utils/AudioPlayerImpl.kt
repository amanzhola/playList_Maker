package com.example.playlistmaker.data.utils

import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import com.example.playlistmaker.domain.api.AudioPlayer
import java.util.Locale

class AudioPlayerImpl : AudioPlayer {

    private var mediaPlayer: MediaPlayer? = null
    private val handler = Handler(Looper.getMainLooper()) // 🚑
    private var onTimeUpdateCallback: ((String) -> Unit)? = null
    private var onStateChangeCallback: ((AudioPlayer.PlaybackState) -> Unit)? = null

    override var currentTrackId: Int = -1
        private set
    override var playbackState: AudioPlayer.PlaybackState = AudioPlayer.PlaybackState.IDLE
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
        private var instance: AudioPlayer? = null

        fun getInstance(): AudioPlayer {
            return instance ?: synchronized(this) {
                instance ?: AudioPlayerImpl().also { instance = it }
            }
        }
    }

    override fun setOnTimeUpdateCallback(callback: (String) -> Unit) { // 🔁 🧩
        this.onTimeUpdateCallback = callback
    }

    override fun setStateChangeCallback(callback: (AudioPlayer.PlaybackState) -> Unit) { // 🔁 🧩
        this.onStateChangeCallback = callback
    }

    override fun setTrack(previewUrl: String, trackId: Int) { // 🎵 ✅ ✨🔄
        stop()
        currentTrackId = trackId
        playbackState = AudioPlayer.PlaybackState.PREPARING
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
                playbackState = AudioPlayer.PlaybackState.PREPARED
                onStateChangeCallback?.invoke(playbackState)
                start() // ▶️ 💃 ⏭️
                playbackState = AudioPlayer.PlaybackState.PLAYING
                onStateChangeCallback?.invoke(playbackState)
                handler.post(updateRunnable)
            }
            setOnCompletionListener {// ⚠️ 📦
                onStateChangeCallback?.invoke(AudioPlayer.PlaybackState.STOPPED)
                onTimeUpdateCallback?.invoke("00:00")
                stop()
            }
            prepareAsync()
        }
        // отправляем статус PREPARING
        onStateChangeCallback?.invoke(AudioPlayer.PlaybackState.PREPARING)
    }

    override fun pause() { // ⏸️
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.pause()
                playbackState = AudioPlayer.PlaybackState.PAUSED
                onStateChangeCallback?.invoke(playbackState)
                handler.removeCallbacks(updateRunnable)
            }
        }
    }

    override fun resume() { // ⏹️ ▶️ + 🛑 00:00
        mediaPlayer?.let {
            if (!it.isPlaying && playbackState == AudioPlayer.PlaybackState.PAUSED) {
                it.start()
                playbackState = AudioPlayer.PlaybackState.PLAYING
                onStateChangeCallback?.invoke(playbackState)
                handler.post(updateRunnable)
            }
        }
    }

    override fun stop() { // 📛
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.stop()
                onStateChangeCallback?.invoke(AudioPlayer.PlaybackState.STOPPED)
                handler.removeCallbacks(updateRunnable)
            }
            release()
        }
        mediaPlayer = null
        currentTrackId = -1
        playbackState = AudioPlayer.PlaybackState.IDLE
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