package com.example.playlistmaker.data.network.movie

import com.example.playlistmaker.data.dto.SearchResponse
import com.example.playlistmaker.data.dto.movie.MovieAdvancedSearchDto
import com.example.playlistmaker.data.dto.movie.MovieSearchDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface IMDbApi {

    // ВАЖНО: encoded=true, чтобы кириллица не «ломалась» в path
    @GET("/en/API/SearchMovie/{apiKey}/{expression}")
    suspend fun searchMovies(
        @Path("apiKey") apiKey: String,
        @Path(value = "expression", encoded = true) expression: String
    ): Response<SearchResponse<MovieSearchDto>>

    @GET("/en/API/AdvancedSearch/{apiKey}/")
    suspend fun getAdvancedSearch(
        @Path("apiKey") apiKey: String,
        @Query("title", encoded = true) title: String
    ): Response<SearchResponse<MovieAdvancedSearchDto>>
}
