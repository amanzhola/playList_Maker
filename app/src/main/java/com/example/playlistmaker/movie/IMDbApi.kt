package com.example.playlistmaker.movie

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface IMDbApi { // 📎 🧑‍💻
    @GET("/en/API/SearchMovie/{apiKey}/{expression}")
    fun findMovie(@Path("apiKey") apiKey: String,
                  @Path("expression") expression: String)
    : Call<MoviesResponse>

    @GET("API/AdvancedSearch/{apiKey}/")
    fun getAdvancedSearch(
        @Path("apiKey") apiKey: String,
        @Query("title") title: String
    ): Call<MoviesResponse>
}
