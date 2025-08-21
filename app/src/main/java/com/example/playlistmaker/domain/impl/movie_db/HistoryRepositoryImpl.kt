package com.example.playlistmaker.domain.impl.movie_db

import com.example.playlistmaker.data.movie_db.MovieDbConvertor
import com.example.playlistmaker.data.movie_db.MoviesDatabase
import com.example.playlistmaker.domain.api.movie_db.HistoryRepository
import com.example.playlistmaker.domain.models.movie.Movie
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map

private const val TAG_REPO = "HistoryRepo"

class HistoryRepositoryImpl(
    private val appDatabase: MoviesDatabase,
    private val movieDbConvertor: MovieDbConvertor,
) : HistoryRepository {

//    companion object { private const val TAG = "HistoryRepo" }

    override fun historyMovies(): Flow<List<Movie>> =
        appDatabase.movieDao()
            .observeMovies()
            .map { entities ->
//                android.util.Log.d(TAG, "observeMovies(): entities.size=${entities.size}")
                entities.map(movieDbConvertor::map)
            }
            .distinctUntilChanged()
            .flowOn(kotlinx.coroutines.Dispatchers.IO)
}
