package com.example.playlistmaker.ui.movie

import android.annotation.SuppressLint
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.playlistmaker.R
import com.example.playlistmaker.domain.models.Movie

class MovieViewHolder(parent: ViewGroup,
                      private val onFavoriteClick: (Movie) -> Unit
    ) :
    RecyclerView.ViewHolder(LayoutInflater.from(parent.context)
        .inflate(R.layout.i_movie, parent, false)) {

    private var cover: ImageView = itemView.findViewById(R.id.cover)
    private var title: TextView = itemView.findViewById(R.id.title)
    private var description: TextView = itemView.findViewById(R.id.description)
    private var inFavoriteToggle: ImageView = itemView.findViewById(R.id.favorite)

    fun bind(movie: Movie) {
        Glide.with(itemView)
            .load(movie.image)
            .into(cover)

        title.text = movie.title
        description.text = movie.description

        //  (❤️)
        inFavoriteToggle.setImageDrawable(getFavoriteToggleDrawable(movie.inFavorite))

        inFavoriteToggle.setOnClickListener {
            onFavoriteClick(movie) //  (❤️)
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables") //  (❤️)
    private fun getFavoriteToggleDrawable(inFavorite: Boolean): Drawable? {
        return itemView.context.getDrawable(
            if(inFavorite) R.drawable.baseline_favorite2_border_24 else R.drawable.baseline_favorite1_border_24
        )
    }
}
