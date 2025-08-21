//package com.example.playlistmaker.domain.repository.movieDetails

//import com.example.playlistmaker.domain.models.movie.Movie
//import com.example.playlistmaker.domain.models.movieDetails.MovieDetails
//import com.example.playlistmaker.domain.util.ResourceMovieDetials
//import kotlinx.coroutines.flow.Flow
//
//interface MoviesRepository {
//    fun searchMovies(expression: String): Flow<ResourceMovieDetials<List<Movie>>>
//    fun getMovieDetails(movieId: String): Flow<ResourceMovieDetials<MovieDetails>>
//}

// no use and doubling Movies Repository at domain -> api -> movie