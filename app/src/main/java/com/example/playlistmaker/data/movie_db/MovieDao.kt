package com.example.playlistmaker.data.movie_db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface MovieDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMovies(movies: List<MovieEntity>)

    @Query("DELETE FROM movie_table")
    suspend fun clearMovies()

    @Transaction
    suspend fun replaceAll(movies: List<MovieEntity>) {
        clearMovies()
        insertMovies(movies)
    }

    // если пользуешься «живой» историей
    @Query("SELECT * FROM movie_table")
    fun observeMovies(): Flow<List<MovieEntity>>
}
