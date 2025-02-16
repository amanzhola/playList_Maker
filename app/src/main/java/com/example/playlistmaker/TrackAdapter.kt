package com.example.playlistmaker

import android.annotation.SuppressLint
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import android.util.TypedValue
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

private const val MAX_ARTIST_NAME_LENGTH = 40

class TrackAdapter(private val tracks: List<Track>) : RecyclerView.Adapter<TrackAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val trackNameTextView: TextView = itemView.findViewById(R.id.track_name)
        private val artistNameTextView: TextView = itemView.findViewById(R.id.track_auctor)
        private val trackTimeTextView: TextView = itemView.findViewById(R.id.track_duration)
        private val artworkImageView: ImageView = itemView.findViewById(R.id.track_image)

        fun bind(track: Track, context: Context) {
            trackNameTextView.text = track.trackName
            if (track.artistName.length > MAX_ARTIST_NAME_LENGTH) {
                ellipsizeEnd(artistNameTextView, MAX_ARTIST_NAME_LENGTH, track.artistName)
            } else {
                artistNameTextView.text = track.artistName
            }
            trackTimeTextView.text = track.trackTime

            val radius = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 2f, itemView.context.resources.displayMetrics
            ).toInt()

            if (isNetworkAvailable(context)) {
                Glide.with(context)
                    .load(track.artworkUrl100)
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
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.track_item, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val track = tracks[position]
        holder.bind(track, holder.itemView.context)
    }

    override fun getItemCount(): Int {
        return tracks.size
    }
}

@SuppressLint("SetTextI18n")
private fun ellipsizeEnd(textView: TextView, maxLength: Int, fullText: String) {
    if (fullText.length <= maxLength) {
        textView.text = fullText
    } else {
        textView.text = fullText.substring(0, maxLength - 3) + "…"
    }
}

private fun isNetworkAvailable(context: Context): Boolean {
    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    val network = connectivityManager.activeNetwork ?: return false
    val networkCapabilities = connectivityManager.getNetworkCapabilities(network) ?: return false

    return networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
            networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
            networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
}