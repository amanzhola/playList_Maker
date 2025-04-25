package com.example.playlist_playertraining

import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import android.util.Log
import java.util.Locale

class AudioPlayer private constructor() {

    private var mediaPlayer: MediaPlayer? = null
    private val handler = Handler(Looper.getMainLooper())
    private var onTimeUpdateCallback: ((String) -> Unit)? = null
    private var stateChangeCallback: ((PlaybackState) -> Unit)? = null
    var currentTrackId: Int = -1
    var lastPlayedTrackId: Int = -1

    enum class PlaybackState {
        IDLE, PREPARING, PREPARED, PLAYING, PAUSED, STOPPED
    }

    var playbackState: PlaybackState = PlaybackState.IDLE

    private val updateRunnable = object : Runnable {
        override fun run() {
            mediaPlayer?.let { player ->
                if (player.isPlaying) {
                    val currentPos = player.currentPosition
                    onTimeUpdateCallback?.invoke(getFormattedTime(currentPos))
                    handler.postDelayed(this, 1000) // Обновление каждую секунду
                }
            }
        }
    }

    companion object {
        @Volatile
        private var instance: AudioPlayer? = null

        fun getInstance(): AudioPlayer {
            return instance ?: synchronized(this) {
                instance ?: AudioPlayer().also { instance = it }
            }
        }
    }

    fun setOnTimeUpdateCallback(callback: (String) -> Unit) {
        onTimeUpdateCallback = callback
    }

    fun setStateChangeCallback(callback: (PlaybackState) -> Unit) {
        stateChangeCallback = callback
    }

    fun setTrack(previewUrl: String, trackId: Int) {
        stopPlayback()
        currentTrackId = trackId
        playbackState = PlaybackState.PREPARING
        stateChangeCallback?.invoke(playbackState)

        mediaPlayer = MediaPlayer().apply {

                setDataSource(previewUrl)
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )

                setOnPreparedListener {
                    Log.d("AudioPlayer", "MediaPlayer is prepared for track ID: $currentTrackId")
                    playbackState = PlaybackState.PREPARED
                    stateChangeCallback?.invoke(playbackState)
                    startPlayback() // Запускаем воспроизведение после подготовки
                }

                setOnCompletionListener {
                    stateChangeCallback?.invoke(PlaybackState.STOPPED)
                    onTimeUpdateCallback?.invoke("00:00")

                    stopPlayback()

                    currentTrackId = -1
                }

                prepareAsync()
        }
    }

    private fun startPlayback() {
        mediaPlayer?.let {
            if (!it.isPlaying) {
                it.start()
                playbackState = PlaybackState.PLAYING
                stateChangeCallback?.invoke(playbackState)
                handler.post(updateRunnable) // Запускаем обновление времени
            }
        }
    }

    fun pause() {
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.pause()
                playbackState = PlaybackState.PAUSED
                stateChangeCallback?.invoke(playbackState)
                handler.removeCallbacks(updateRunnable) // Останавливаем обновление времени
            }
        }
    }

    fun resume() {
        mediaPlayer?.let {
            if (!it.isPlaying && playbackState == PlaybackState.PAUSED) {
                it.start()
                playbackState = PlaybackState.PLAYING
                stateChangeCallback?.invoke(playbackState)
                handler.post(updateRunnable) // Возвращаем обновление времени
            }
        }
    }

    fun stopPlayback() {
        mediaPlayer?.let {
            it.stop()
            playbackState = PlaybackState.STOPPED
            stateChangeCallback?.invoke(playbackState)
            handler.removeCallbacks(updateRunnable)
            releasePlayer() // Освобождение ресурсов после остановки
        }
    }

    private fun releasePlayer() {
        mediaPlayer?.release()
        mediaPlayer = null
        currentTrackId = -1
        playbackState = PlaybackState.IDLE
        stateChangeCallback?.invoke(playbackState)
    }

    fun isPlaying(): Boolean = mediaPlayer?.isPlaying == true

    fun isCurrentTrackPlaying(trackId: Int): Boolean {
        return isPlaying() && currentTrackId == trackId
    }

    private fun getFormattedTime(milliseconds: Int): String {
        val totalSeconds = milliseconds / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
    }

    fun getValidTrackId(): Int {
        return if (currentTrackId != -1) currentTrackId else lastPlayedTrackId
    }
}