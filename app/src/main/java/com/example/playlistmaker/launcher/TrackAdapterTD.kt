package com.example.playlistmaker.launcher

import android.content.Context
import android.content.Intent
import android.util.TypedValue
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
import com.example.playlistmaker.search.OnTrackClickListener
import com.example.playlistmaker.search.Track
import com.example.playlistmaker.search.TracksDiffCallback
import com.google.android.material.imageview.ShapeableImageView
import com.google.gson.Gson

class TrackAdapterTD(
    private var tracks: List<Track>,
    private val listener: OnTrackClickListener
) : RecyclerView.Adapter<TrackAdapterTD.ViewHolder>() {

    fun updateTracks(newTracks: List<Track>) {
        val diffCallback = TracksDiffCallback(tracks, newTracks)
        val diffResult = DiffUtil.calculateDiff(diffCallback)

        tracks = newTracks
        diffResult.dispatchUpdatesTo(this)
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val trackNameTextView: TextView = itemView.findViewById(R.id.track_name)
        private val artistNameTextView: TextView = itemView.findViewById(R.id.track_author)
        private val trackTimeTextView: TextView = itemView.findViewById(R.id.track_duration)
        private val artworkImageView: ImageView = itemView.findViewById(R.id.track_image)
        private val arrowImageView: ShapeableImageView = itemView.findViewById(R.id.arrow_fw)

        fun bind(track: Track, context: Context) {
            trackNameTextView.text = track.trackName
            artistNameTextView.text = track.artistName
            trackTimeTextView.text = track.trackDuration

            val radius = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                2f,
                context.resources.displayMetrics
            ).toInt()

            Glide.with(context)
                .load(track.artworkUrlSmall)
                .placeholder(R.drawable.placeholder)
                .transform(RoundedCorners(radius))
                .into(artworkImageView)

            itemView.setOnClickListener {

                val intent = Intent(context, TrackPreviewActivity::class.java).apply {
                    putExtra("track", track)
                    putExtra("track_list_json", Gson().toJson(tracks))
                    putExtra("track_index", bindingAdapterPosition)
                }
                context.startActivity(intent)
            }

            arrowImageView.setOnClickListener {
                listener.onArrowClicked(track)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.track_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val track = tracks[position]
        holder.bind(track, holder.itemView.context)
    }

    override fun getItemCount(): Int = tracks.size
}
