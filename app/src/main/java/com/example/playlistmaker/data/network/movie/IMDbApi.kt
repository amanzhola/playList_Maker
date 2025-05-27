package com.example.playlistmaker.data.network.movie

import com.example.playlistmaker.data.dto.SearchResponse
import com.example.playlistmaker.data.dto.movie.MovieAdvancedSearchDto
import com.example.playlistmaker.data.dto.movie.MovieSearchDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query


interface IMDbApi {
    @GET("/en/API/SearchMovie/{apiKey}/{expression}")
    suspend fun searchMovies(
        @Path("apiKey") apiKey: String,
        @Path("expression") expression: String
    ): Response<SearchResponse<MovieSearchDto>>

    @GET("API/AdvancedSearch/{apiKey}/")
    suspend fun getAdvancedSearch(
        @Path("apiKey") apiKey: String,
        @Query("title") title: String
    ): Response<SearchResponse<MovieAdvancedSearchDto>>
}