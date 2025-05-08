package com.example.playlistmaker.data.network

import com.example.playlistmaker.data.dto.AudioSearchResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface ITunesApi {
    @GET("/search?entity=song")
    suspend fun search(@Query("term") text: String): Response<AudioSearchResponse>
}

//package com.example.playlistmaker.data.network
//
//import com.example.playlistmaker.data.dto.AudioSearchResponse
//import retrofit2.Call
//import retrofit2.Retrofit
//import retrofit2.converter.gson.GsonConverterFactory
//import retrofit2.http.GET
//import retrofit2.http.Query
//
//interface ITunesApi {
//    @GET("/search?entity=song")
//    fun search(@Query("term") text: String): Call<AudioSearchResponse>
//
//    companion object {
//        fun create(): ITunesApi {
//            val retrofit = Retrofit.Builder()
//                .baseUrl("https://itunes.apple.com")
//                .addConverterFactory(GsonConverterFactory.create())
//                .build()
//            return retrofit.create(ITunesApi::class.java)
//        }
//    }
//}