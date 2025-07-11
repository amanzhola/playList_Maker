package com.example.playlistmaker.data.dto.movieCast.response

data class WritersResponse(
    val items: List<CastItemResponse>,
    val job: String
)