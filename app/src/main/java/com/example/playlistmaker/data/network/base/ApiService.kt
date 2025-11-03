package com.example.playlistmaker.data.network.base

import retrofit2.Response
import retrofit2.http.GET

interface ApiService { //  👨‍💻✨
    // лёгкий запрос, возвращает 204 No Content, используется самим Android для проверки интернета
    @GET("generate_204")
    suspend fun health(): Response<Unit>
}