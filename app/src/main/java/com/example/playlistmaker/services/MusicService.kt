package com.example.playlistmaker.services

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import com.example.playlistmaker.R
import com.example.playlistmaker.domain.api.player.AudioPlayerControl
import com.example.playlistmaker.domain.api.player.PlaybackState
import com.example.playlistmaker.domain.api.player.PlayerUiState
import com.example.playlistmaker.utils.BUTTON_TEXT_PAUSE
import com.example.playlistmaker.utils.BUTTON_TEXT_PLAY
import com.example.playlistmaker.utils.CHANNEL_ID
import com.example.playlistmaker.utils.CHANNEL_NAME
import com.example.playlistmaker.utils.EXTRA_ARTIST
import com.example.playlistmaker.utils.EXTRA_ID
import com.example.playlistmaker.utils.EXTRA_TITLE
import com.example.playlistmaker.utils.EXTRA_URL
import com.example.playlistmaker.utils.NOTIF_ID
import com.example.playlistmaker.utils.TIMER_INTERVAL_MS
import com.example.playlistmaker.utils.ZERO_TIME
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale


class MusicService : Service(), AudioPlayerControl {

    private var startAfterPrepare: Boolean = false

    // ─────────── Binder ───────────
    inner class MusicServiceBinder : Binder() {
        fun getService(): MusicService = this@MusicService
    }
    private val binder = MusicServiceBinder()

    // ─────────── State ───────────
    private var inForeground = false
    private val _ui = MutableStateFlow(PlayerUiState(
        isButtonEnabled = false,
        isPlaying = false,
        progress = ZERO_TIME,
        buttonText = BUTTON_TEXT_PLAY
    ))
    private val _state = MutableStateFlow(PlaybackState.IDLE)
    override fun getPlayerState(): StateFlow<PlayerUiState> = _ui.asStateFlow()
    override fun getPlaybackState(): StateFlow<PlaybackState> = _state.asStateFlow()

    private var mediaPlayer: MediaPlayer? = null
    private var timerJob: Job? = null

    private var url: String? = null
    private var artist: String? = null
    private var title: String? = null
    override var currentTrackId: Int? = null
        private set

    @SuppressLint("ObsoleteSdkInt")
    private fun ensureChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            if (nm.getNotificationChannel(CHANNEL_ID) == null) {
                nm.createNotificationChannel(
                    NotificationChannel(
                        CHANNEL_ID,
                        CHANNEL_NAME,
                        NotificationManager.IMPORTANCE_LOW
                    ).apply {
                        setSound(null, null)
                        enableVibration(false)
                    }
                )
            }
        }
    }

    private fun buildNotification(): Notification {
        val text = "${artist.orEmpty()} - ${title.orEmpty()}".trim().trim('-')
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.queue_music_24) // замени при желании
            .setContentTitle(getString(R.string.app_name))
            .setContentText(text.ifEmpty { getString(R.string.playback_in_progress) })
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .build()
    }

    // ─────────── Service lifecycle ───────────
    override fun onCreate() {
        super.onCreate()
        ensureChannel()
        mediaPlayer = MediaPlayer()
    }

    override fun onBind(intent: Intent?): IBinder {
        // читаем extras — это «привязка по чек-листу»
        intent?.let {
            url = it.getStringExtra(EXTRA_URL)
            artist = it.getStringExtra(EXTRA_ARTIST)
            title = it.getStringExtra(EXTRA_TITLE)
            currentTrackId = it.getIntExtra(EXTRA_ID, -1).takeIf { id -> id >= 0 }
        }
        initMediaPlayerIfNeeded()
        return binder
    }

    override fun onUnbind(intent: Intent?): Boolean {
        return true // чтобы при повторной привязке прилетел onRebind
    }

    override fun onRebind(intent: Intent?) {
        // ничего особого — UI вернулся
        super.onRebind(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        releasePlayer()
    }

    // ─────────── AudioPlayerControl impl ───────────
    override fun setTrack(url: String, trackId: Int, artist: String, title: String) {
        this.url = url
        this.currentTrackId = trackId
        this.artist = artist
        this.title = title
        // моментально обнуляем прогресс в UI для нового currentTrackId
        _ui.value = _ui.value.copy(progress = ZERO_TIME, isPlaying = false, buttonText = BUTTON_TEXT_PLAY)
        resetAndPrepare(url)
    }

    override fun startPlayer() {
        val mp = mediaPlayer ?: return
        when (_state.value) {
            PlaybackState.PREPARED, PlaybackState.PAUSED -> {
                try {
                    mp.start()
                    _state.value = PlaybackState.PLAYING
                    _ui.value = _ui.value.copy(isButtonEnabled = true, isPlaying = true, buttonText = BUTTON_TEXT_PAUSE)
                    startTimer()
                } catch (e: IllegalStateException) {
                    _state.value = PlaybackState.ERROR
                    _ui.value = PlayerUiState(
                        isButtonEnabled = false,
                        isPlaying = false,
                        progress = ZERO_TIME,
                        buttonText = BUTTON_TEXT_PLAY
                    )
                    stopForegroundNow(true)
                }
            }
            PlaybackState.COMPLETED -> {
                try {
                    mp.seekTo(0)
                    mp.start()
                    _state.value = PlaybackState.PLAYING
                    _ui.value = _ui.value.copy(isButtonEnabled = true, isPlaying = true, buttonText = BUTTON_TEXT_PAUSE)
                    startTimer()
                } catch (_: IllegalStateException) {
                    _state.value = PlaybackState.ERROR
                    _ui.value = PlayerUiState(
                        isButtonEnabled = false,
                        isPlaying = false,
                        progress = ZERO_TIME,
                        buttonText = BUTTON_TEXT_PLAY
                    )
                    stopForegroundNow(true)
                }
            }
            PlaybackState.PREPARING -> {
                // ⬅️ пользователь нажал "Play" до подготовки — пометим, что нужно авто-стартануть
                startAfterPrepare = true
            }
            else -> Unit // IDLE/ERROR/STOPPED — для текущей логики стартим через setTrack()
        }
    }

    override fun pausePlayer() {
        val mp = mediaPlayer ?: return
        try {
            if (mp.isPlaying) {
                mp.pause()
                _state.value = PlaybackState.PAUSED
                stopTimer()
                _ui.value = _ui.value.copy(isButtonEnabled = true, isPlaying = false, buttonText = BUTTON_TEXT_PLAY)
            }
        } catch (e: IllegalStateException) {
            _state.value = PlaybackState.ERROR
            _ui.value = PlayerUiState(
                isButtonEnabled = false,
                isPlaying = false,
                progress = ZERO_TIME,
                buttonText = BUTTON_TEXT_PLAY
            )
            stopTimer()
            stopForegroundNow(true)
        }
    }

    override fun stopPlayer() {
        startAfterPrepare = false
        val mp = mediaPlayer
        stopTimer()
        try {
            mp?.stop()
        } catch (_: IllegalStateException) {
            // ок, уже не в состоянии STOP-able
        }
        try {
            mp?.reset()
        } catch (_: IllegalStateException) { }
        _state.value = PlaybackState.IDLE
        _ui.value = PlayerUiState(isButtonEnabled = false, isPlaying = false, progress = ZERO_TIME, buttonText = BUTTON_TEXT_PLAY)
        stopForegroundNow(true)
    }

    override fun startForegroundNow() {
        // запускаем уведомление только если реально идёт воспроизведение
        // и мы ещё не в foreground (чтобы не дублировать startForeground)
        if (_state.value == PlaybackState.PLAYING && !inForeground) {
            ensureChannel()
            ServiceCompat.startForeground(
                this,
                NOTIF_ID,
                buildNotification(),
                if (Build.VERSION.SDK_INT >= 34)
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
                else 0
            )
            inForeground = true
        }
    }

    override fun stopForegroundNow(cancelNotification: Boolean) {
        // Всегда снимаем системно, без ручного cancel(): так надёжнее.
        // STOP_FOREGROUND_REMOVE сам удалит уведомление.
        ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
        inForeground = false
    }

    // ─────────── MediaPlayer helpers ───────────
    private fun initMediaPlayerIfNeeded() {
        val u = url ?: return
        if (mediaPlayer == null) mediaPlayer = MediaPlayer()
        if (_state.value == PlaybackState.IDLE || _state.value == PlaybackState.ERROR) {
            resetAndPrepare(u)
        }
    }

    @SuppressLint("ObsoleteSdkInt")
    private fun resetAndPrepare(u: String) {
        stopTimer()
        mediaPlayer?.reset()
        _state.value = PlaybackState.PREPARING
        _ui.value = _ui.value.copy(isButtonEnabled = false, isPlaying = false, progress = ZERO_TIME, buttonText = BUTTON_TEXT_PLAY)

        try {
            mediaPlayer?.apply {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    setAudioAttributes(
                        AudioAttributes.Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .build()
                    )
                }

                setDataSource(u)

                setOnPreparedListener {
                    _state.value = PlaybackState.PREPARED

                    if (startAfterPrepare) {
                        startAfterPrepare = false
                        startPlayer()
                    } else {
                        _ui.value = _ui.value.copy(
                            isButtonEnabled = true,
                            progress = ZERO_TIME,
                            buttonText = BUTTON_TEXT_PLAY
                        )
                    }
                }

                setOnCompletionListener {
                    stopTimer()
                    _state.value = PlaybackState.COMPLETED
                    _ui.value = PlayerUiState(
                        isButtonEnabled = true,
                        isPlaying = false,
                        progress = ZERO_TIME,
                        buttonText = BUTTON_TEXT_PLAY
                    )
                    stopForegroundNow(true)
                }

                setOnErrorListener { _, _, _ ->
                    stopTimer()
                    _state.value = PlaybackState.ERROR
                    _ui.value = PlayerUiState(
                        isButtonEnabled = false,
                        isPlaying = false,
                        progress = ZERO_TIME,
                        buttonText = BUTTON_TEXT_PLAY
                    )
                    stopForegroundNow(true)
                    true
                }

                prepareAsync()
            }
        } catch (t: Throwable) {
            _state.value = PlaybackState.ERROR
            _ui.value = PlayerUiState(
                isButtonEnabled = false,
                isPlaying = false,
                progress = ZERO_TIME,
                buttonText = BUTTON_TEXT_PLAY
            )
            stopForegroundNow(true)
        }
    }

    private fun startTimer() {
        stopTimer()
        timerJob = CoroutineScope(Dispatchers.Default).launch {
            while (isActive && mediaPlayer?.isPlaying == true) {
                delay(TIMER_INTERVAL_MS)
                val p = mediaPlayer?.currentPosition ?: 0
                val formatted = SimpleDateFormat("mm:ss", Locale.getDefault()).format(p)
                _ui.value = _ui.value.copy(progress = formatted)
            }
        }
    }

    private fun stopTimer() {
        timerJob?.cancel()
        timerJob = null
    }

    private fun releasePlayer() {
        startAfterPrepare = false
        stopTimer()
        // 🔽 важно: сначала убираем foreground при необходимости
        if (inForeground) {
            stopForegroundNow(true)
        }
        mediaPlayer?.setOnPreparedListener(null)
        mediaPlayer?.setOnCompletionListener(null)
        try { mediaPlayer?.release() } catch (_: Exception) {}
        mediaPlayer = null
    }
}
