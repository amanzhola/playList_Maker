package com.example.playlistmaker.data.network.wiki

import okhttp3.Interceptor
import okhttp3.Response

class UserAgentInterceptor(
    private val userAgent: String
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val req = chain.request()
            .newBuilder()
            .header("User-Agent", userAgent) // критично для Wiki
            .build()
        return chain.proceed(req)
    }
}
