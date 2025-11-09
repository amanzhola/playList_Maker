package com.example.playlistmaker.data.dto.wiki

data class WikiLangResponse(
    val query: Query? = null
) {
    data class Query(
        val pages: List<Page> = emptyList()
    )

    data class Page(
        val pageid: Int? = null,
        val title: String? = null,
        val langlinks: List<LangLink> = emptyList()
    )

    data class LangLink(
        val lang: String,
        val title: String
    )
}
