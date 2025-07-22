package com.example.playlistmaker.data.network.movieDetails

import com.example.playlistmaker.data.dto.movieCast.response.MovieCastResponse
import com.example.playlistmaker.data.dto.movieDetails.MovieDetailsResponse
import com.example.playlistmaker.data.dto.movieDetails.MoviesSearchResponse
import com.example.playlistmaker.data.dto.moviePersons.NamesSearchResponse
import retrofit2.http.GET
import retrofit2.http.Path

interface IMDbApiService {

    @GET("/en/API/SearchName/k_zcuw1ytf/{expression}")
    suspend fun searchNames(@Path("expression") expression: String): NamesSearchResponse

    @GET("/en/API/SearchMovie/k_zcuw1ytf/{expression}")
    suspend fun searchMovies(@Path("expression") expression: String): MoviesSearchResponse

    @GET("/en/API/Title/k_zcuw1ytf/{movie_id}")
    suspend fun getMovieDetails(@Path("movie_id") movieId: String): MovieDetailsResponse

    @GET("/en/API/FullCast/k_zcuw1ytf/{movie_id}")
    suspend fun getFullCast(@Path("movie_id") movieId: String): MovieCastResponse
}