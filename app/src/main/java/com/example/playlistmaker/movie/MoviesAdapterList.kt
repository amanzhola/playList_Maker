package com.example.playlistmaker.movie

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.playlistmaker.R

class MoviesAdapterList(private val movies: List<Movie>,
                        private val orientationToggle: () -> Unit,
                        private val activity: AppCompatActivity) : RecyclerView.Adapter<MoviesAdapterList.MovieViewHolder>() {

    private var isVertical: Boolean = false

    class MovieViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val menuButton: ImageButton = itemView.findViewById(R.id.menu_button)
        val title: TextView = itemView.findViewById(R.id.title)
        val cover: ImageView = itemView.findViewById(R.id.cover)
        val genre: TextView = itemView.findViewById(R.id.genre)
        val rating: TextView = itemView.findViewById(R.id.rating)
        val description: TextView = itemView.findViewById(R.id.description)
        val direction: ImageButton = itemView.findViewById(R.id.directionButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MovieViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.i_movie1, parent, false)
        return MovieViewHolder(view)
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onBindViewHolder(holder: MovieViewHolder, position: Int) {
        val movie = movies[position]
        holder.title.text = movie.title

        holder.menuButton.setOnClickListener { activity.finish() }

        Glide.with(holder.itemView.context)
            .load(movie.image)
            .placeholder(R.drawable.placeholder2)
            .error(R.drawable.placeholder2)
            .into(holder.cover)

        holder.genre.text = movie.genres ?: "Жанр не указан"
        val formattedRating = "${movie.imDbRating ?: "N/A"}/10"
        holder.rating.text = formattedRating
        val description = "${movie.description}\n${movie.plot ?: ""}"
        holder.description.text = description

        holder.direction.setImageResource(
            if (isVertical) R.drawable.vertical_24 else R.drawable.horizontal_24
        )

        holder.direction.setOnClickListener {
            isVertical = !isVertical  // Переключаем orientation
            orientationToggle() // Вызываем коллбэк для изменения ориентации
            notifyDataSetChanged()
        }

    }

    override fun getItemCount(): Int {
        return movies.size
    }
}