package com.example.playlistmaker.ui.audioPosters

import android.annotation.SuppressLint
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer // ⬇️ + импорты
import androidx.media3.ui.DefaultTimeBar
import androidx.media3.ui.PlayerView // ⬇️ + импорты
import androidx.media3.ui.TimeBar
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.playlistmaker.R
import com.example.playlistmaker.domain.models.search.Track
import com.example.playlistmaker.ui.widgets.PlaybackButtonView
import com.example.playlistmaker.utils.TracksDiffCallbackAudio


interface OnTrackAudioClickListener {
    fun onTrackClicked(track: Track, position: Int)
    fun onPlayButtonClicked(track: Track)
    fun onBackArrowClicked()
    // 🆕 Новый метод для клика по кнопке "Избранное"
    fun onFavoriteClicked(track: Track) // ❤️
    fun onAddTrackClicked(track: Track) // 🎵➕👉💿

    // 🆕 seek по аудио-ползунку
    fun onSeekRequested(track: Track, positionMs: Long)
}

class TrackAdapterAudio( // ⚠️ ViewBinding 🚫 ➡️ 📉 📈 📛
    private var tracks: List<Track>,
    private val listener: OnTrackAudioClickListener,
    private val layoutId: Int = R.layout.track_item1
) : RecyclerView.Adapter<TrackAdapterAudio.TrackViewHolder>() {

    private var timebarVertical: Boolean = true // по умолчанию: вертикальный (т.к. горизонтальный список)

    // for audio time bar + video time bar
    fun setTimebarVertical(isVertical: Boolean) {
        if (timebarVertical != isVertical) {
            timebarVertical = isVertical
            // аудио-таймбары — у всех видимых // точечное обновление: пэйлоад "timebarOrientation"
            notifyItemRangeChanged(0, itemCount, listOf("timebarOrientation"))
            // видео-таймбар — только у позиции, где прикреплено видео
            if (videoPos != RecyclerView.NO_POSITION) {
                notifyItemChanged(videoPos, listOf("videoTimebarOrientation"))
            }
        }
    }

    fun update(newItems: List<Track>) { // 🌟 💖
        val diffCallback = TracksDiffCallbackAudio(tracks, newItems)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        tracks = newItems
        diffResult.dispatchUpdatesTo(this)
    }

    @UnstableApi
    @SuppressLint("ClickableViewAccessibility")
    inner class TrackViewHolder @OptIn(UnstableApi::class) constructor
        (itemView: View) : RecyclerView.ViewHolder(itemView) {

        // 🔁 постер и PlayerView для audio
        private val audioTimeBar: DefaultTimeBar? = itemView.findViewById(R.id.audio_timebar)

        // 🔁 постер и PlayerView для video
        private val poster: ImageView = itemView.findViewById(R.id.track_image)
        val playerView: PlayerView? = itemView.findViewById(R.id.player_view)

        private val trackImage: ImageView = itemView.findViewById(R.id.track_image)
        private val trackName: TextView = itemView.findViewById(R.id.track_name)
        private val authorName: TextView = itemView.findViewById(R.id.track_author)
        private val trackTime: TextView = itemView.findViewById(R.id.track_duration)
        private val album: TextView = itemView.findViewById(R.id.album)
        private val trackAlbum: TextView = itemView.findViewById(R.id.track_album)
        private val trackYear: TextView = itemView.findViewById(R.id.track_year)
        private val trackGenre: TextView = itemView.findViewById(R.id.track_genre)
        private val trackCountry: TextView = itemView.findViewById(R.id.track_country)
        private val playButton: PlaybackButtonView = itemView.findViewById(R.id.play_track)
        private val playTime: TextView = itemView.findViewById(R.id.play_time)
        private val backArrow: ImageView? = itemView.findViewById(R.id.arrow_back)
        private val favorite: ImageView? = itemView.findViewById(R.id.favorite)
        private val addTrack: ImageView? = itemView.findViewById(R.id.add_track)

        init {

            // ▶️ колбэк кастомной вью
            playButton.onToggleRequested = { _ ->
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    listener.onPlayButtonClicked(tracks[position])
                }
            }

            // 🎵 Клик по элементу списка
            itemView.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    listener.onTrackClicked(tracks[position], position)
                }
            }

            // ⬅️ Назад
            backArrow?.setOnClickListener {
                if (bindingAdapterPosition != RecyclerView.NO_POSITION) {
                    listener.onBackArrowClicked()
                }
            }

            // ❤️ Новый обработчик для избранного
            favorite?.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    listener.onFavoriteClicked(tracks[position])
                }
            }

            // 🎵➕ Add Track 👉💿
            addTrack?.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    listener.onAddTrackClicked(tracks[position])
                }
            }

            // стало (минимально):
            playerView?.useController = true
            // По желанию, чтобы таймбар всегда виден:
             playerView?.controllerShowTimeoutMs = 0

            configurePlayerViewOnce()

            audioTimeBar?.addListener(object : TimeBar.OnScrubListener {
                override fun onScrubStart(timeBar: TimeBar, position: Long) {}
                override fun onScrubMove(timeBar: TimeBar, position: Long) {}
                override fun onScrubStop(timeBar: TimeBar, position: Long, canceled: Boolean) {
                    val idx = bindingAdapterPosition
                    if (!canceled && idx != RecyclerView.NO_POSITION) {
                        listener.onSeekRequested(tracks[idx], position)
                    }
                }
            })

        }

        fun bind(track: Track) {

            // По умолчанию показываем постер (видео прячем)
            hideVideo()

            Glide.with(itemView.context)
                .load(track.artworkUrl512)
                .placeholder(R.drawable.placeholder)
                .transform(RoundedCorners(8))
                .into(trackImage)

            trackName.text = track.trackName
            authorName.text = track.artistName
            trackTime.text = track.trackDuration

            if (track.collectionName.isEmpty()) {
                trackAlbum.visibility = View.GONE
                album.visibility = View.GONE
            } else {
                trackAlbum.text = track.collectionName
                trackAlbum.visibility = View.VISIBLE
                album.visibility = View.VISIBLE
            }

            val date = track.releaseDate
            val cutFrom = date.indexOf('-').takeIf { it >= 0 } ?: date.length
            trackYear.text = date.replaceRange(cutFrom, date.length, "")

            trackGenre.text = track.primaryGenreName
            trackCountry.text = track.country

            updatePlayTime(track)
            updatePlayState(track)

            // ❤️ Обновляем иконку избранного
            favorite?.setImageResource(
                if (track.isFavorite) R.drawable.favorite1 else R.drawable.favorite
            )
        }

        fun updatePlayTime(track: Track) {
            playTime.text = track.playTime ?: "0:00"
        }

        fun updatePlayState(track: Track) {
            playButton.setPlaying(track.isPlaying)
        }

        // ⬇️ ВКЛЮЧИТЬ видео в этом холдере
        @OptIn(UnstableApi::class)
        fun showVideo(player: ExoPlayer) {
            playerView?.useController = true   // на всякий случай

            playerView?.player = player
            playerView?.visibility = View.VISIBLE

            playerView?.showController()

            poster.visibility = View.GONE

            hideAudioTimebar()              // ⬅️ audio time bar
        }

        // ⬇️ ВЫКЛЮЧИТЬ видео в этом холдере
        fun hideVideo() {
            // важно отвязать player, чтобы избежать «утечки» вьюхи
            playerView?.player = null
            playerView?.visibility = View.GONE
            poster.visibility = View.VISIBLE
        }

        // только статические поля (тексты, иконки, playTime/isPlaying) — БЕЗ постера/видео
        fun bindTexts(track: Track) {
            trackName.text = track.trackName
            authorName.text = track.artistName
            trackTime.text = track.trackDuration

            if (track.collectionName.isEmpty()) {
                trackAlbum.visibility = View.GONE
                album.visibility = View.GONE
            } else {
                trackAlbum.text = track.collectionName
                trackAlbum.visibility = View.VISIBLE
                album.visibility = View.VISIBLE
            }

            val date = track.releaseDate
            val cutFrom = date.indexOf('-').takeIf { it >= 0 } ?: date.length
            trackYear.text = date.replaceRange(cutFrom, date.length, "")

            trackGenre.text = track.primaryGenreName
            trackCountry.text = track.country

            updatePlayTime(track)
            updatePlayState(track)

            favorite?.setImageResource(
                if (track.isFavorite) R.drawable.favorite1 else R.drawable.favorite
            )
        }

        // только постер (когда нет видео)
        fun bindPoster(track: Track) {
            Glide.with(itemView.context)
                .load(track.artworkUrl512)
                .placeholder(R.drawable.placeholder)
                .transform(RoundedCorners(8))
                .into(trackImage)
        }

        fun setPlayTimeText(text: String) {
            playTime.text = text
        }

        fun showAudioTimebar(positionMs: Long, durationMs: Long, bufferedMs: Long) {
            audioTimeBar?.apply {
                visibility = View.VISIBLE
                setDuration(if (durationMs > 0) durationMs else 0L)
                setBufferedPosition(bufferedMs.coerceAtLeast(0))
                setPosition(positionMs.coerceAtLeast(0))
            }
        }

        fun hideAudioTimebar() {
            audioTimeBar?.visibility = View.GONE
        }

        // for audio time bar
        fun applyTimebarOrientation(vertical: Boolean) {
            audioTimeBar ?: return
            val tb = audioTimeBar
            val lp = tb.layoutParams as FrameLayout.LayoutParams

            if (vertical) {
                // справа, снизу-вверх
                tb.rotation = 270f
                tb.pivotX = 0f
                tb.pivotY = 0f
                lp.width = FrameLayout.LayoutParams.MATCH_PARENT
                lp.height = dp(12)
                lp.gravity = Gravity.BOTTOM

            } else {
                // горизонтальный снизу
                tb.rotation = 0f
                tb.pivotX = 0f
                tb.pivotY = 0f
                tb.translationX = 0f
                lp.width = FrameLayout.LayoutParams.MATCH_PARENT
                lp.height = dp(12)
                lp.gravity = Gravity.TOP
            }
            tb.layoutParams = lp
        }

        fun applyVideoTimebarOrientation(vertical: Boolean) {
            val pv = playerView ?: return
            // ВАЖНО: искать системный id из Media3
            pv.post {
                val tb = pv.findViewById<androidx.media3.ui.DefaultTimeBar?>(
                    androidx.media3.ui.R.id.exo_progress
                ) ?: return@post

                val lp = (tb.layoutParams as? FrameLayout.LayoutParams)
                    ?: FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        dp(12)
                    )

                if (vertical) {
                    // Вертикально справа, прогресс снизу-вверх
                    tb.rotation = 270f
                    tb.pivotX = 0f
                    tb.pivotY = 0f
                    tb.translationX = 0f

                    lp.width  = FrameLayout.LayoutParams.MATCH_PARENT
                    lp.height = dp(12)
                    lp.gravity = Gravity.BOTTOM
                } else {
                    // Горизонтально (сверху/снизу — на выбор)
                    tb.rotation = 0f
                    tb.pivotX = 0f
                    tb.pivotY = 0f
                    tb.translationX = 0f

                    lp.width  = FrameLayout.LayoutParams.MATCH_PARENT
                    lp.height = dp(12)
                    lp.gravity = Gravity.TOP // или Gravity.BOTTOM
                }
                tb.layoutParams = lp

                // чтобы точно видно сразу
                pv.useController = true
                pv.controllerShowTimeoutMs = 0
                pv.showController()
            }
        }

        private fun dp(v: Int): Int {
            val d = itemView.resources.displayMetrics.density
            return (v * d + 0.5f).toInt()
        }
    }

    @OptIn(UnstableApi::class)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrackViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(layoutId, parent, false)
        return TrackViewHolder(view)
    }

    @OptIn(UnstableApi::class)
    override fun onBindViewHolder(holder: TrackViewHolder, position: Int) {
        val track = tracks[position]

        // audio time bar
        holder.applyTimebarOrientation(timebarVertical)

        // 1) всегда сначала тексты
        holder.bindTexts(track)

        // 2) визуальная часть: видео или постер
        if (position == videoPos && currentPlayer != null) {
            holder.showVideo(currentPlayer!!)
            holder.applyVideoTimebarOrientation(timebarVertical)
            holder.hideAudioTimebar()            // ⬅️ при видео таймбар аудио скрыт
        } else {
            holder.hideVideo()      // спрятать PlayerView
            holder.bindPoster(track) // показать постер
            holder.hideAudioTimebar()            // ⬅️ по умолчанию скрываем аудио-таймбар
            // Показ и обновление таймбара для текущей карточки сделает фрагмент через
            // adapter.updateAudioTimebarAt(...) при каждом тике прогресса.
        }

    }

    @OptIn(UnstableApi::class)
    override fun onBindViewHolder(holder: TrackViewHolder, position: Int, payloads: MutableList<Any>) {
        if (payloads.isEmpty()) {
            onBindViewHolder(holder, position)
        } else {
            val payload = payloads[0] as? List<*>
            val track = tracks[position]

            payload?.forEach {
                when (it) {
                    "playTime" -> holder.updatePlayTime(track)
                    "isPlaying" -> holder.updatePlayState(track)
                    // ➕
                    "favorite"   -> {
                        holder.itemView.findViewById<ImageView?>(R.id.favorite)?.setImageResource(
                            if (track.isFavorite) R.drawable.favorite1 else R.drawable.favorite
                        )
                    }
                    "timebarOrientation" -> holder.applyTimebarOrientation(timebarVertical)
                    "videoTimebarOrientation" -> holder.applyVideoTimebarOrientation(timebarVertical)
                }
            }
        }
    }

    // ⬇️ ПРИ РЕЦИКЛЕ — обязательно скрываем видео и отвязываем player
    @OptIn(UnstableApi::class)
    override fun onViewRecycled(holder: TrackViewHolder) {
        holder.hideVideo()
        holder.hideAudioTimebar() // audio time bar
        super.onViewRecycled(holder)
    }

    override fun getItemCount(): Int = tracks.size

    fun getItems(): List<Track> = tracks

    // 🔧 Хелперы для фрагмента: прикрепить/открепить видео к позиции
    @OptIn(UnstableApi::class)
    fun attachVideoAt(recycler: RecyclerView, position: Int, player: ExoPlayer) {
        val vh = recycler.findViewHolderForAdapterPosition(position) as? TrackViewHolder ?: return
        vh.showVideo(player)
    }

    @OptIn(UnstableApi::class)
    fun detachVideoAt(recycler: RecyclerView, position: Int) {
        val vh = recycler.findViewHolderForAdapterPosition(position) as? TrackViewHolder ?: return
        vh.hideVideo()
    }

    private var videoPos: Int = RecyclerView.NO_POSITION
    private var currentPlayer: ExoPlayer? = null

    fun setVideoBoundPosition(pos: Int) {
        val old = videoPos
        videoPos = pos
        if (old != RecyclerView.NO_POSITION) notifyItemChanged(old)
        notifyItemChanged(pos) // попросим onBind у обеих позиций отрисовать верное состояние
    }

    fun clearVideoBoundPosition() {
        val old = videoPos
        videoPos = RecyclerView.NO_POSITION
        if (old != RecyclerView.NO_POSITION) notifyItemChanged(old)
    }

    fun setVideoPlayer(player: ExoPlayer?) {
        currentPlayer = player
    }

    @OptIn(UnstableApi::class)
    fun updatePlayTimeTextAt(recycler: RecyclerView, position: Int, text: String) {
        val vh = recycler.findViewHolderForAdapterPosition(position) as? TrackViewHolder ?: return
        vh.setPlayTimeText(text)
    }

    @OptIn(UnstableApi::class)
    fun updateAudioTimebarAt(
        recycler: RecyclerView,
        position: Int,
        positionMs: Long,
        durationMs: Long,
        bufferedMs: Long
    ) {
        val vh = recycler.findViewHolderForAdapterPosition(position) as? TrackViewHolder ?: return
        // Показываем ползунок только если ЭТО НЕ видео-карточка
        if (position != videoPos || currentPlayer == null) {
            vh.showAudioTimebar(positionMs, durationMs, bufferedMs)
        } else {
            vh.hideAudioTimebar()
        }
    }

    @OptIn(UnstableApi::class)
    private fun TrackViewHolder.configurePlayerViewOnce() {
        playerView?.apply {
            useController = true                 // включаем контроллер
            controllerShowTimeoutMs = 0          // не скрывать
            // Можно и это оставить (на всякий случай):
            // setControllerAutoShow(true)       // auto-show, хотя уже есть в XML
        }
    }

    // audio time bar
    @OptIn(UnstableApi::class)
    fun hideAudioTimebarForVisibleExcept(recycler: RecyclerView, keepPos: Int) {
        val lm = recycler.layoutManager as? LinearLayoutManager ?: return
        val first = lm.findFirstVisibleItemPosition()
        val last  = lm.findLastVisibleItemPosition()
        if (first == RecyclerView.NO_POSITION || last == RecyclerView.NO_POSITION) return

        for (pos in first..last) {
            val vh = recycler.findViewHolderForAdapterPosition(pos) as? TrackViewHolder ?: continue
            // если это видео-карточка — там свой контроллер, таймбар аудио скрываем
            if (pos == videoPos && currentPlayer != null) {
                vh.hideAudioTimebar()
                continue
            }
            if (pos == keepPos) {
                // оставим как есть — актуальные значения выставит updateAudioTimebarAt(...)
            } else {
                // для всех НЕ текущих — прячем (или можно обнулить, см. функцию ниже)
                vh.hideAudioTimebar()
            }
        }
    }

    // audio time bar
    /** Если не прятать, а именно «обнулять» ползунок у НЕ текущих карточек */
    @OptIn(UnstableApi::class)
    fun zeroAudioTimebarForVisibleExcept(recycler: RecyclerView, keepPos: Int) {
        val lm = recycler.layoutManager as? LinearLayoutManager ?: return
        val first = lm.findFirstVisibleItemPosition()
        val last  = lm.findLastVisibleItemPosition()
        if (first == RecyclerView.NO_POSITION || last == RecyclerView.NO_POSITION) return

        for (pos in first..last) {
            val vh = recycler.findViewHolderForAdapterPosition(pos) as? TrackViewHolder ?: continue
            if (pos == videoPos && currentPlayer != null) {
                vh.hideAudioTimebar()
            } else if (pos != keepPos) {
                // duration=0 ⇒ «неактивный» нулевой ползунок
                vh.showAudioTimebar(positionMs = 0L, durationMs = 0L, bufferedMs = 0L)
            }
        }
    }

}
