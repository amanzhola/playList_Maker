package com.example.playlistmaker.ui.movie.movieHistory

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.playlistmaker.domain.models.movie.Movie

class HistoryAdapter : RecyclerView.Adapter<HistoryViewHolder>() {

    var movies = ArrayList<Movie>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder = HistoryViewHolder(parent)

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        holder.bind(movies.get(position))
    }

    override fun getItemCount(): Int = movies.size
}