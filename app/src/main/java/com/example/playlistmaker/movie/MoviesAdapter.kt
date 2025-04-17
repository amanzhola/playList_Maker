package com.example.playlistmaker.movie

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView

sealed class MoviesEvent {
    data class SingleMovie(val movie: Movie, val position: Int) : MoviesEvent()
    data class MovieList(val movies: List<Movie>, val position: Int) : MoviesEvent()
}

class MoviesAdapter(private val onItemClicked: (MoviesEvent) -> Unit) : RecyclerView.Adapter<MovieViewHolder>() {

    var movies = ArrayList<Movie>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MovieViewHolder {
        return MovieViewHolder(parent)
    }

    override fun onBindViewHolder(holder: MovieViewHolder, position: Int) {
        val movie = movies[position]
        holder.bind(movie)

        holder.itemView.setOnClickListener {
            onItemClicked(MoviesEvent.SingleMovie(movie, position))
        }
    }

    override fun getItemCount(): Int = movies.size

    fun updateMovies(newMovies: List<Movie>) {
        val diffCallback = MoviesDiffCallback(movies, newMovies)
        val diffResult = DiffUtil.calculateDiff(diffCallback)

//        movies = newMovies as ArrayList<Movie>
//        diffResult.dispatchUpdatesTo(this)

        movies.clear() // Очищаем старый список
        movies.addAll(newMovies) // Добавляем новый, перевернутый список
        diffResult.dispatchUpdatesTo(this)
    }

    fun getMovies(): List<Movie> = movies.toList() // Возвращаем неизменяемый список
}