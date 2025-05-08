package com.example.playlistmaker.ui.audioPosters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.playlistmaker.R
import com.example.playlistmaker.domain.models.Track
import com.example.playlistmaker.utils.TracksDiffCallbackAudio

interface OnTrackAudioClickListener {
    fun onTrackClicked(track: Track, position: Int)
    fun onPlayButtonClicked(track: Track)
    fun onBackArrowClicked()
}

class TrackAdapterAudio(
    private var tracks: List<Track>,
    private val listener: OnTrackAudioClickListener,
    private val layoutId: Int = R.layout.track_item1
) : RecyclerView.Adapter<TrackAdapterAudio.TrackViewHolder>() {

    fun update(newItems: List<Track>) {
        val diffCallback = TracksDiffCallbackAudio(tracks, newItems)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        tracks = newItems
        diffResult.dispatchUpdatesTo(this)
    }

    inner class TrackViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val trackImage: ImageView = itemView.findViewById(R.id.track_image)
        private val trackName: TextView = itemView.findViewById(R.id.track_name)
        private val authorName: TextView = itemView.findViewById(R.id.track_author)
        private val trackTime: TextView = itemView.findViewById(R.id.track_duration)
        private val album: TextView = itemView.findViewById(R.id.album)
        private val trackAlbum: TextView = itemView.findViewById(R.id.track_album)
        private val trackYear: TextView = itemView.findViewById(R.id.track_year)
        private val trackGenre: TextView = itemView.findViewById(R.id.track_genre)
        private val trackCountry: TextView = itemView.findViewById(R.id.track_country)
        private val playButton: ImageView = itemView.findViewById(R.id.play_track)
        private val playTime: TextView = itemView.findViewById(R.id.play_time)
        private val backArrow: ImageView? = itemView.findViewById(R.id.arrow_back)

        init {
            playButton.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    listener.onPlayButtonClicked(tracks[position])
                }
            }
            itemView.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    listener.onTrackClicked(tracks[position], position)
                }
            }
            backArrow?.setOnClickListener {
                Log.d("TrackAdapter", "backArrow: $backArrow")
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    listener.onBackArrowClicked()
                }
            }
        }

        fun bind(track: Track) {
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

            trackYear.text = track.releaseDate.replaceRange(
                track.releaseDate.indexOf("-"),
                track.releaseDate.length,
                ""
            )
            trackGenre.text = track.primaryGenreName
            trackCountry.text = track.country

            updatePlayTime(track)
            updatePlayState(track)
        }

        fun updatePlayTime(track: Track) {
            playTime.text = track.playTime ?: "🕒0:00"
        }

        fun updatePlayState(track: Track) {
            playButton.setImageResource(
                if (track.isPlaying) R.drawable.pause else R.drawable.play
            )
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrackViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(layoutId, parent, false)
        return TrackViewHolder(view)
    }

    override fun onBindViewHolder(holder: TrackViewHolder, position: Int) {
        holder.bind(tracks[position])
    }

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
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return tracks.size
    }
}