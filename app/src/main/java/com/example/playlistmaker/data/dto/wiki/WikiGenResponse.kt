package com.example.playlistmaker.data.dto.wiki

data class WikiGenResponse(
    val query: Query? = null,
    val `continue`: Continue? = null
) {
    data class Query(
        val pages: List<Page> = emptyList()
    )

    data class Page(
        val pageid: Int,
        val title: String,
        // короткое описание (часто на EN, на RU бывает пусто — тогда возьмём extract)
        val description: String? = null,
        // короткая выжимка (экстракт) — plaintext
        val extract: String? = null,
        val thumbnail: Thumbnail? = null
    )

    data class Thumbnail(
        val source: String,
        val width: Int,
        val height: Int
    )

    // для пагинации (если решишь юзать)
    data class Continue(
        val gsroffset: Int? = null,
        val `continue`: String? = null
    )
}
