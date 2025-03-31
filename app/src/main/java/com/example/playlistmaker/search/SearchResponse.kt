package com.example.playlistmaker.search

data class SearchResponse(
    val resultCount: Int,
    val results: MutableList<Track>
)
