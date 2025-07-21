package com.example.playlistmaker.ui.movie

import android.annotation.SuppressLint
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.playlistmaker.domain.models.movie.Movie
import com.example.playlistmaker.utils.GenericDiffCallback

sealed class MoviesEvent {
    data class SingleMovie(val movie: Movie, val position: Int) : MoviesEvent()
    data class MovieList(val movies: List<Movie>, val position: Int) : MoviesEvent()
}
class MoviesAdapter(private val onItemClicked: (MoviesEvent) -> Unit,
                    private val onFavoriteClicked: (Movie) -> Unit
    ) : RecyclerView.Adapter<MovieViewHolder>() {

//    private val debounce = Debounce_handler(1000L) // ⛔ 🕒 1 секунда задержки
    var movies = ArrayList<Movie>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MovieViewHolder {
        return MovieViewHolder(parent, onFavoriteClicked)
    }

    override fun onBindViewHolder(holder: MovieViewHolder, position: Int) {
        val movie = movies[position]
        holder.bind(movie)

        holder.itemView.setOnClickListener {

            onItemClicked(MoviesEvent.SingleMovie(movie, position))

//            debounce.debounce {
//                onItemClicked(MoviesEvent.SingleMovie(movie, position))
//            }
        }
    }

    override fun getItemCount(): Int = movies.size

    @SuppressLint("NotifyDataSetChanged")
    fun updateMovies(newMovies: List<Movie>) {
        val diffCallback = GenericDiffCallback(movies, newMovies)
        val diffResult = DiffUtil.calculateDiff(diffCallback)

        movies.clear()
        movies.addAll(newMovies)
        diffResult.dispatchUpdatesTo(this)
    }

    fun getMovies(): List<Movie> = movies.toList()
}
