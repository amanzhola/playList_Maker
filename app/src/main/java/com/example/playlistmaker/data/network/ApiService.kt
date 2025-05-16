package com.example.playlistmaker.data.network

import retrofit2.Call
import retrofit2.http.GET

interface ApiService { //  👨‍💻✨
    @GET("/") // Путь к ресурсу
    fun checkInternetConnection(): Call<Void>
}