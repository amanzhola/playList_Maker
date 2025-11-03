package com.example.playlistmaker.data.network.base

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitInstance { // 📝
    // 🔗
    private const val BASE_URL = "https://clients3.google.com/" // ✅ правильный и лёгкий вариант

    private val okHttp = OkHttpClient.Builder()
        .connectTimeout(1, TimeUnit.SECONDS)
        .readTimeout(1, TimeUnit.SECONDS)
        .writeTimeout(1, TimeUnit.SECONDS)
        .callTimeout(2, TimeUnit.SECONDS)
        .build()

    private val retrofit by lazy { // 🎵🎚️🚀
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttp)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    // 👨‍💻
    val api: ApiService by lazy { retrofit.create(ApiService::class.java) }
}
