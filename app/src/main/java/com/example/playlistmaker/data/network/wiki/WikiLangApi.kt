// data/network/wiki/WikiLangApi.kt
package com.example.playlistmaker.data.network.wiki

import com.example.playlistmaker.data.dto.wiki.WikiLangResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface WikiLangApi {
    // один вызов, как в твоём успешном примере в браузере
    @GET(
        "w/api.php" +
                "?action=query" +
                "&format=json" +
                "&formatversion=2" +
                "&utf8=1" +
                "&generator=search" +
                "&gsrlimit=1" +          // только лучший матч
                "&gsrnamespace=0" +      // статьи
                "&prop=langlinks" +
                "&lllang=en" +
                "&redirects=1"           // на всякий — следовать редиректам
    )
    suspend fun ruToEnTitle(
        @Query("gsrsearch") query: String
    ): WikiLangResponse
}
