@file:OptIn(androidx.media3.common.util.UnstableApi::class)
@file:Suppress("OPT_IN_ARGUMENT_IS_NOT_MARKER")

package com.example.playlistmaker.ui.audioPosters.adapter

import android.content.Context
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalConfiguration
import androidx.media3.exoplayer.ExoPlayer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.playlistmaker.R
import com.example.playlistmaker.domain.models.search.Track
import com.example.playlistmaker.utils.TracksDiffCallbackAudio

// ---------- интерфейс кликов ----------
interface OnTrackAudioClickListener {
    fun onTrackClicked(track: Track, position: Int)
    fun onPlayButtonClicked(track: Track)
    fun onBackArrowClicked()
    fun onFavoriteClicked(track: Track)
    fun onAddTrackClicked(track: Track)
    fun onSeekRequested(track: Track, positionMs: Long)
}

// ---------- адаптер с ComposeView во ViewHolder ----------
class TrackAdapterAudio(
    private var tracks: List<Track>,
    private val listener: OnTrackAudioClickListener,
    @LayoutRes private val layoutId: Int = R.layout.track_item1,
) : RecyclerView.Adapter<TrackAdapterAudio.ComposeVH>() {

    // ❗ Всё «живое» — в Compose state, чтобы не вызывать notify… во время скролла
    private val timebarVerticalState = mutableStateOf(true)
    private val videoPosState = mutableStateOf(RecyclerView.NO_POSITION)
    private val currentPlayerState = mutableStateOf<ExoPlayer?>(null)

    // overlay для текста времени (для ВИДЕО)
    private val playTimeOverlay = mutableStateMapOf<Int, String>()

    // прогресс аудио-полосы (только для НЕ видео)
    data class AudioProgress(val positionMs: Long, val durationMs: Long, val bufferedMs: Long)
    private val audioProgressByPos = mutableStateMapOf<Int, AudioProgress>()

    private val style: ItemStyle = ItemStyle.fromLayout(layoutId)

    enum class ItemStyle {
        TRACK_ITEM_1,
        TRACK_ITEM_2;
        companion object {
            fun fromLayout(@LayoutRes id: Int): ItemStyle =
                if (id == R.layout.track_item2) TRACK_ITEM_2 else TRACK_ITEM_1
        }
    }

    fun update(newItems: List<Track>) {
        val diffCallback = TracksDiffCallbackAudio(tracks, newItems)
        val diff = DiffUtil.calculateDiff(diffCallback)
        tracks = newItems
        diff.dispatchUpdatesTo(this)
    }

    fun getItems(): List<Track> = tracks

    inner class ComposeVH(val compose: ComposeView) : RecyclerView.ViewHolder(compose)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ComposeVH {
        val cv = ComposeView(parent.context).apply {
            layoutParams = RecyclerView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }
        return ComposeVH(cv)
    }

    override fun onBindViewHolder(holder: ComposeVH, position: Int) {
        // чтобы «резинка» по вертикали не схлопывалась
        val params = holder.itemView.layoutParams
        if (params.height != ViewGroup.LayoutParams.MATCH_PARENT) {
            params.height = ViewGroup.LayoutParams.MATCH_PARENT
            holder.itemView.layoutParams = params
        }
        bindCompose(holder, position)
    }

    override fun onBindViewHolder(holder: ComposeVH, position: Int, payloads: MutableList<Any>) {
        val params = holder.itemView.layoutParams
        if (params.height != ViewGroup.LayoutParams.MATCH_PARENT) {
            params.height = ViewGroup.LayoutParams.MATCH_PARENT
            holder.itemView.layoutParams = params
        }
        bindCompose(holder, position)
    }

    private fun bindCompose(holder: ComposeVH, position: Int) {
        val track = tracks[position]

        holder.compose.setContent {
            MaterialTheme {
                val isVideo = position == videoPosState.value && currentPlayerState.value != null
                val overlayTime = playTimeOverlay[position]
                val audioProgress = audioProgressByPos[position]

                TrackItemCompose(
                    context = holder.compose.context,
                    track = track,
                    isVideoAttached = isVideo,
                    timebarVertical = timebarVerticalState.value,
                    currentPlayer = currentPlayerState.value,
                    overlayPlayTime = overlayTime,
                    audioProgress = audioProgress,
                    onPlayToggle = { listener.onPlayButtonClicked(track) },
                    onItemClick = { listener.onTrackClicked(track, position) },
                    onBack = { listener.onBackArrowClicked() },
                    onFav = { listener.onFavoriteClicked(track) },
                    onAdd = { listener.onAddTrackClicked(track) },
                    onSeek = { ms -> listener.onSeekRequested(track, ms) },
                    style = style,
                )
            }
        }
    }

    override fun getItemCount(): Int = tracks.size
}

//*********************************************************

@Composable
fun TrackItemCompose(
    context: Context,
    track: Track,
    isVideoAttached: Boolean,
    timebarVertical: Boolean,
    currentPlayer: ExoPlayer?,
    overlayPlayTime: String?,
    audioProgress: Any?,
    onPlayToggle: () -> Unit,
    onItemClick: () -> Unit,
    onBack: () -> Unit,
    onFav: () -> Unit,
    onAdd: () -> Unit,
    onSeek: (Long) -> Unit,
    style: TrackAdapterAudio.ItemStyle,
    textColorOverride: androidx.compose.ui.graphics.Color? = null,
    textColorOverrideAux: androidx.compose.ui.graphics.Color? = null,
    iconTintOverride:  androidx.compose.ui.graphics.Color? = null,
    iconTintOverrideAux:  androidx.compose.ui.graphics.Color? = null,
) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE

    if (isLandscape) {
        TrackItemComposeLand(
            context, track, isVideoAttached, timebarVertical, currentPlayer,
            overlayPlayTime, audioProgress, onPlayToggle,
            onItemClick, onBack, onFav, onAdd, onSeek, style,
            textColorOverride, textColorOverrideAux, iconTintOverride, iconTintOverrideAux
        )
    } else {
        TrackItemComposePortrait(
            context, track, isVideoAttached, timebarVertical, currentPlayer,
            overlayPlayTime, audioProgress, onPlayToggle, onItemClick,
            onBack, onFav, onAdd, onSeek, style,
            textColorOverride, textColorOverrideAux, iconTintOverride, iconTintOverrideAux
        )
    }
}