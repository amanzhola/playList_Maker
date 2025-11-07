package com.example.playlistmaker.data.network.wiki

import com.example.playlistmaker.data.dto.wiki.WikiGenResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface WikiApi {

    /**
     * Ищем страницы через generator=search и сразу просим:
     *  - pageimages (thumbnail)
     *  - description (краткое)
     *  - extracts (короткий plaintext-анонс)
     */
    @GET(
        "w/api.php" +
                "?action=query" +
                "&format=json" +
                "&formatversion=2" +
                "&utf8=1" +
                "&generator=search" +
                "&gsrlimit=20" +                 // сколько результатов
                "&gsrnamespace=0" +              // только статьи
                "&prop=pageimages|description|extracts" +
                "&piprop=thumbnail" +
                "&pithumbsize=240" +             // размер превью (px по ширине)
                "&pilicense=any" +               // брать любые лицензии (иначе часть не вернёт)
                "&exintro=1" +                   // только вводную часть
                "&explaintext=1" +               // без HTML
                "&exchars=180"                   // ограничим длину для списка
    )
    suspend fun searchWithThumbs(
        @Query("gsrsearch") query: String,
        @Query("gsroffset") offset: Int? = null // для пагинации (необязательно)
    ): WikiGenResponse
}
