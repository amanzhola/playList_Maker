package com.example.playlistmaker.domain.impl.movie_db

import com.example.playlistmaker.data.movie_db.MovieDbConvertor
import com.example.playlistmaker.data.movie_db.MovieEntity
import com.example.playlistmaker.data.movie_db.MoviesDatabase
import com.example.playlistmaker.domain.api.movie_db.HistoryRepository
import com.example.playlistmaker.domain.models.movie.Movie
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow


class HistoryRepositoryImpl(
    private val appDatabase: MoviesDatabase,
    private val movieDbConvertor: MovieDbConvertor,
) : HistoryRepository {

    override fun historyMovies(): Flow<List<Movie>> = flow {
        val movies = appDatabase.movieDao().getMovies()
        emit(convertFromMovieEntity(movies))
    }

    private fun convertFromMovieEntity(movies: List<MovieEntity>): List<Movie> {
        return movies.map { movie -> movieDbConvertor.map(movie) }
    }
}