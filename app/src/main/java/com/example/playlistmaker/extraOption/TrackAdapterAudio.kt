package com.example.playlistmaker.extraOption

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.playlistmaker.R
import com.example.playlistmaker.search.Track

interface OnTrackAudioClickListener {
    fun onTrackClicked(track: Track)
}

class TrackAdapterAudio(
    private val tracks: List<Track>,
    private val listener: OnTrackAudioClickListener
) : RecyclerView.Adapter<TrackAdapterAudio.TrackViewHolder>() {

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

        init {
            itemView.setOnClickListener {
                listener.onTrackClicked(tracks[adapterPosition])
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
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrackViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.track_item1, parent, false)
        return TrackViewHolder(view)
    }

    override fun onBindViewHolder(holder: TrackViewHolder, position: Int) {
        holder.bind(tracks[position])
    }

    override fun getItemCount(): Int {
        return tracks.size
    }
}