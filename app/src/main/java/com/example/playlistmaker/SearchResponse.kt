package com.example.playlistmaker

data class SearchResponse(
    val resultCount: Int,
    val results: List<Track>
)