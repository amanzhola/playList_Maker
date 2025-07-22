package com.example.playlistmaker.data.repository.player

import android.media.AudioAttributes
import android.media.MediaPlayer
import com.example.playlistmaker.domain.api.player.AudioPlayerInteraction
import com.example.playlistmaker.domain.api.player.PlaybackState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.isActive
import java.util.Locale

// 🚀 NEW Coroutine-based AudioPlayerInteractionImpl
class AudioPlayerInteractionImpl : AudioPlayerInteraction {

    private var mediaPlayer: MediaPlayer? = null // 📖 🎶

    override var currentTrackId: Int = -1
    override var lastPlayedTrackId: Int = -1

    private val _playbackState = MutableStateFlow(PlaybackState.IDLE) // 🔁 🧩
    override val playbackState: StateFlow<PlaybackState> get() = _playbackState

    override val playTime: Flow<String> = flow {  // 🔁 🧩 🚀
        while (currentCoroutineContext().isActive) { // 📖 🎶
            mediaPlayer?.let { player ->
                if (player.isPlaying) {
                    emit(getFormattedTime(player.currentPosition)) // 🌼
                }
            }
//            delay(1000)
            delay(300)
        }
    }.flowOn(Dispatchers.Main)

    override fun setTrack(previewUrl: String, trackId: Int) { // 🎵 ✅ ✨🔄
        stopPlayback()  // 📛
        currentTrackId = trackId
        _playbackState.value = PlaybackState.PREPARING

        mediaPlayer = MediaPlayer().apply {// 📖 🎶
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .build()
            )
            setDataSource(previewUrl)

            setOnPreparedListener { // 📌
                _playbackState.value = PlaybackState.PREPARED
                startPlayback() // ▶️ 💃 ⏭️
            }

            setOnCompletionListener {
                stopPlayback() // 🛑 ❓ 🔁 🧩
            }

            setOnErrorListener { _, _, _ -> // 🛑 ❓ 🔁 🧩
                _playbackState.value = PlaybackState.IDLE
                releasePlayer() // 🧹
                true
            }

            prepareAsync()
        }
    }

    private fun startPlayback() { // ▶️ 💃 ⏭️
        mediaPlayer?.let {
            if (!it.isPlaying) {
                it.start()
                _playbackState.value = PlaybackState.PLAYING
            }
        }
    }

    override fun pause() { // ⏸️
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.pause()
                _playbackState.value = PlaybackState.PAUSED
            }
        }
    }

    override fun resume() { // ⏹️ ▶️ + 🛑
        mediaPlayer?.let {
            if (!it.isPlaying && _playbackState.value == PlaybackState.PAUSED) {
                it.start()
                _playbackState.value = PlaybackState.PLAYING
            }
        }
    }

    override fun stopPlayback() { // 📛
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.stop()
                _playbackState.value = PlaybackState.STOPPED
            }
            releasePlayer() // 🧹
        }
    }

    private fun releasePlayer() { // 🧹
        mediaPlayer?.release()
        mediaPlayer = null
        currentTrackId = -1
        _playbackState.value = PlaybackState.IDLE
    }

    override fun isPlaying(): Boolean = mediaPlayer?.isPlaying == true // ☕

    override fun isCurrentTrackPlaying(trackId: Int): Boolean = // ⛷️
        isPlaying() && currentTrackId == trackId

    override fun getValidTrackId(): Int = // 🔥 100%
        if (currentTrackId != -1) currentTrackId else lastPlayedTrackId

    private fun getFormattedTime(milliseconds: Int): String { // 🌼
        val totalSeconds = milliseconds / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
    }

    // clean auto since by view model life coroutine close preventing memory leaks //  🤘
    // 💤 OLD Callback-based AudioPlayerInteractionImpl
}


