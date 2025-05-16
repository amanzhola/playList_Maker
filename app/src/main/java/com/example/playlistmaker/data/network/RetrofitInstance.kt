package com.example.playlistmaker.data.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


object RetrofitInstance { // 📝
    private const val BASE_URL = "https://www.google.com/" // 🔗

    private val retrofit by lazy { // 🎵🎚️🚀
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val api: ApiService by lazy { // 👨‍💻
        retrofit.create(ApiService::class.java)
    }
}