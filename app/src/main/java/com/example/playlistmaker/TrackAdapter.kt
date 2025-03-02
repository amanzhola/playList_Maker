package com.example.playlistmaker

import android.annotation.SuppressLint
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import android.util.TypedValue
import android.content.Context
import android.content.res.ColorStateList
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.imageview.ShapeableImageView

class TrackAdapter(private val tracks: List<Track>, context: Context) : RecyclerView.Adapter<TrackAdapter.ViewHolder>() {

    private val defaultTextColor: Int = context.getColor(R.color.hintColor_white)
    private val defaultTextNameColor: Int = context.getColor(R.color.black_white)
    private var textNameColor: Int = defaultTextNameColor
    private var textColor: Int = defaultTextColor
    private var arrowColor: Int = defaultTextColor

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val trackNameTextView: TextView = itemView.findViewById(R.id.track_name)
        private val artistNameTextView: TextView = itemView.findViewById(R.id.track_auctor)
        private val trackTimeTextView: TextView = itemView.findViewById(R.id.track_duration)
        private val artworkImageView: ImageView = itemView.findViewById(R.id.track_image)
        private val arrowImageView: ShapeableImageView = itemView.findViewById(R.id.arrow_fw)

        fun bind(track: Track, context: Context, arrowColor: Int, textColor: Int, textNameColor: Int) {
            trackNameTextView.text = track.trackName
            artistNameTextView.text = track.artistName
            trackTimeTextView.text = track.trackDuration

            val radius: Int = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 2f, itemView.context.resources.displayMetrics
            ).toInt()

            if (isNetworkAvailable(context)) {

                Glide.with(context)
                    .load(track.artworkUrl100)
                    .placeholder(R.drawable.placeholder)
                    .transform(RoundedCorners(radius))
                    .into(artworkImageView)
            } else {
                Glide.with(context)
                    .load(R.drawable.placeholder)
                    .error(R.drawable.placeholder)
                    .transform(RoundedCorners(radius))
                    .into(artworkImageView)
            }

            arrowImageView.imageTintList = ColorStateList.valueOf(arrowColor)
            trackNameTextView.setTextColor(textNameColor)
            artistNameTextView.setTextColor(textColor)
            trackTimeTextView.setTextColor(textColor)

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView: View = LayoutInflater.from(parent.context).inflate(R.layout.track_item, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val track: Track = tracks[position]
        holder.bind(track, holder.itemView.context, arrowColor, textColor, textNameColor)
    }

    override fun getItemCount(): Int {
        return tracks.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setArrowColor(color: Int) {
        this.arrowColor = color
        notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setTextColor(color: Int) {
        this.textNameColor = color
        this.textColor = color
        notifyDataSetChanged()
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