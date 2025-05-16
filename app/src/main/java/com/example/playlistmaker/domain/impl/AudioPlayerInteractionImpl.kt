package com.example.playlistmaker.domain.impl

import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import com.example.playlistmaker.domain.api.AudioPlayerInteraction
import com.example.playlistmaker.domain.api.PlaybackState
import java.util.Locale

class AudioPlayerInteractionImpl : AudioPlayerInteraction {

    private var mediaPlayer: MediaPlayer? = null
    private val handler = Handler(Looper.getMainLooper()) // 🚑
    private var onTimeUpdateCallback: ((String) -> Unit)? = null
    private var stateChangeCallback: ((PlaybackState) -> Unit)? = null


    override var currentTrackId: Int = -1
    override var lastPlayedTrackId: Int = -1
    override var playbackState: PlaybackState = PlaybackState.IDLE

    private val updateRunnable = object : Runnable {
        override fun run() {
            mediaPlayer?.let { player -> // 📖 🎶
                if (player.isPlaying) {
                    val currentPos = player.currentPosition
                    onTimeUpdateCallback?.invoke(getFormattedTime(currentPos))
                    handler.postDelayed(this, 1000) // 💬
                }
            }
        }
    }

    override fun setOnTimeUpdateCallback(callback: (String) -> Unit) { // 🔁 🧩
        onTimeUpdateCallback = callback
    }

    override fun setStateChangeCallback(callback: (PlaybackState) -> Unit) { // 🔁 🧩
        stateChangeCallback = callback
    }

    override fun setTrack(previewUrl: String, trackId: Int) { // 🎵 ✅ ✨🔄
        stopPlayback()
        currentTrackId = trackId
        playbackState = PlaybackState.PREPARING
        stateChangeCallback?.invoke(playbackState)

        mediaPlayer = MediaPlayer().apply { // 💡 ⏭️
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .build()
            )

            setDataSource(previewUrl)

            setOnPreparedListener { // 📌
                playbackState = PlaybackState.PREPARED
                stateChangeCallback?.invoke(playbackState)
                startPlayback()
            }

            setOnCompletionListener {
                stopPlayback() //  🛑 ❓ 🔁 🧩
            }

            setOnErrorListener { _, what, extra -> // ⚠️ 📦
                playbackState = PlaybackState.IDLE
                stateChangeCallback?.invoke(playbackState)
                releasePlayer()
                true
            }

            prepareAsync()
        }

    }

    private fun startPlayback() { // ▶️ 💃 ⏭️
        mediaPlayer?.let {
            if (!it.isPlaying) {
                it.start()
                playbackState = PlaybackState.PLAYING
                stateChangeCallback?.invoke(playbackState)
                handler.post(updateRunnable)
            }
        }
    }

    override fun pause() { // ⏸️
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.pause()
                playbackState = PlaybackState.PAUSED
                stateChangeCallback?.invoke(playbackState)
                handler.removeCallbacks(updateRunnable)
            }
        }
    }

    override fun resume() { // ⏹️ ▶️ + 🛑 00:00
        mediaPlayer?.let {
            if (!it.isPlaying && playbackState == PlaybackState.PAUSED) {
                it.start()
                playbackState = PlaybackState.PLAYING
                stateChangeCallback?.invoke(playbackState)
                handler.post(updateRunnable) // 🧵 🤓
            }
        }
    }

    override fun stopPlayback() { // 📛
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.stop()
                playbackState = PlaybackState.STOPPED
                stateChangeCallback?.invoke(playbackState)
                handler.removeCallbacks(updateRunnable)
            }
            releasePlayer()
        }
    }

    private fun releasePlayer() { // 🧹
        mediaPlayer?.release()
        mediaPlayer = null
        currentTrackId = -1
        playbackState = PlaybackState.IDLE
        stateChangeCallback?.invoke(playbackState)

        Handler(Looper.getMainLooper()).postDelayed({
            stateChangeCallback?.invoke(playbackState)
        }, 100) //  🛑 ❓ 🔁 🧩
    }

    override fun isPlaying(): Boolean = mediaPlayer?.isPlaying == true // ☕

    override fun isCurrentTrackPlaying(trackId: Int): Boolean { // ⛷️
        return isPlaying() && currentTrackId == trackId
    }

    private fun getFormattedTime(milliseconds: Int): String { // 🌼
        val totalSeconds = milliseconds / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
    }

    override fun getValidTrackId(): Int { // 🔥 100%
        return if (currentTrackId != -1) currentTrackId else lastPlayedTrackId
    }

    override fun clearCallbacks() { //  🤘
        onTimeUpdateCallback = null
        stateChangeCallback = null
        handler.removeCallbacks(updateRunnable)
    }
}