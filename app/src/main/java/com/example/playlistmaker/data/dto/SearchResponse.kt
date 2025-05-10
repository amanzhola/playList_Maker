package com.example.playlistmaker.data.dto

data class SearchResponse<T>(
    val results: List<T>?,
    val searchType: String?,
    val expression: String?,
    val errorMessage: String?
)