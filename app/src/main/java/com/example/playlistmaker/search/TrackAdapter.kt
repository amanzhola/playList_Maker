package com.example.playlistmaker.search

import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
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
import com.example.playlistmaker.ExtraOption
import com.example.playlistmaker.R
import com.example.playlistmaker.search.NetworkUtils.isNetworkAvailable
import com.google.android.material.imageview.ShapeableImageView
import com.google.gson.Gson

interface OnTrackClickListener {
    fun onArrowClicked(track: Track)
    fun onTrackClicked(track: Track)
}

class TrackAdapter(private var tracks: MutableList<Track>,
                   context: Context,
                   private val listener: OnTrackClickListener
) :
    RecyclerView.Adapter<TrackAdapter.ViewHolder>() {

    private val defaultTextColor: Int = context.resources.getColor(R.color.hintColor_white, context.theme)
    private val defaultTextNameColor: Int = context.resources.getColor(R.color.black_white, context.theme)
    private var textNameColor: Int = defaultTextNameColor
    private var textColor: Int = defaultTextColor
    private var arrowColor: Int = defaultTextColor

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val trackNameTextView: TextView = itemView.findViewById(R.id.track_name)
        private val artistNameTextView: TextView = itemView.findViewById(R.id.track_author)
        private val trackTimeTextView: TextView = itemView.findViewById(R.id.track_duration)
        private val artworkImageView: ImageView = itemView.findViewById(R.id.track_image)
        val arrowImageView: ShapeableImageView = itemView.findViewById(R.id.arrow_fw)

        fun bind(track: Track, context: Context, arrowColor: Int, textColor: Int, textNameColor: Int) {
            trackNameTextView.text = track.trackName
            artistNameTextView.text = track.artistName
            trackTimeTextView.text = track.trackDuration

            val radius: Int = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 2f, itemView.context.resources.displayMetrics
            ).toInt()

            if (isNetworkAvailable(context)) {

                Glide.with(context)
                    .load(track.artworkUrlSmall)
                    .placeholder(R.drawable.placeholder)
                    .transform(
                        RoundedCorners(radius)
                    )
                    .into(artworkImageView)
            } else {
                Glide.with(context)
                    .load(R.drawable.placeholder)
                    .error(R.drawable.placeholder)
                    .transform(
                        RoundedCorners(radius)
                    )
                    .into(artworkImageView)
            }

            arrowImageView.imageTintList = ColorStateList.valueOf(arrowColor)
            trackNameTextView.setTextColor(textNameColor)
            artistNameTextView.setTextColor(textColor)
            trackTimeTextView.setTextColor(textColor)
        }
    }


    fun getTracks(): List<Track> {
        return tracks
    }

    fun updateTracks(newTracks: MutableList<Track>) {
        val diffCallback = TracksDiffCallback(tracks, newTracks)
        val diffResult = DiffUtil.calculateDiff(diffCallback)

        this.tracks.clear()
        this.tracks.addAll(newTracks)

        diffResult.dispatchUpdatesTo(this)
    }

    fun reverseTracks() { // 🔍
        val reversedTracks = tracks.reversed().toMutableList()
        updateTracks(reversedTracks)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView: View = LayoutInflater.from(parent.context).inflate(R.layout.track_item, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val track: Track = tracks[position]
        holder.bind(track, holder.itemView.context, arrowColor, textColor, textNameColor)

        holder.arrowImageView.setOnClickListener {
            listener.onArrowClicked(track)
        }

        holder.itemView.setOnClickListener {

            listener.onTrackClicked(track)
            val context = holder.itemView.context
            val trackListJson = Gson().toJson(tracks)

            val intent = Intent(context, ExtraOption::class.java).apply {
                putExtra("TRACK_LIST_JSON", trackListJson)
                putExtra("TRACK_INDEX", holder.bindingAdapterPosition)
                putExtra("IS_FROM_SEARCH", true)
            }
            context.startActivity(intent)
        }

// as option to return for a page only 👈 alternative via viewpager2 page/4direct_list movie
//        holder.itemView.setOnClickListener {
//            listener.onTrackClicked(track)
//            val intent = Intent(holder.itemView.context, ExtraOption::class.java).apply {
//                putExtra("TRACK_DATA", track)
//            }
//            holder.itemView.context.startActivity(intent)
//        }
    }

    override fun getItemCount(): Int {
        return tracks.size
    }

    fun setArrowColor(color: Int) {
        this.arrowColor = color
        notifyItemRangeChanged(0, itemCount) // 🧐
    }

    fun setTextColor(color: Int) {
        this.textColor = color
        this.textNameColor = color
        notifyItemRangeChanged(0, itemCount) // 🧐
    }

}