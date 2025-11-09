// data/network/wiki/UserAgentInterceptor.kt
package com.example.playlistmaker.data.network.wiki

import okhttp3.Interceptor
import okhttp3.Response

class UserAgentInterceptor(
    private val ua: String = "PlaylistMaker/1.0 (support@example.com)"
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        return chain.proceed(
            chain.request().newBuilder()
                .header("User-Agent", ua)
                .build()
        )
    }
}
