package com.example.playlistmaker.data.dto.movieCast.response

data class DirectorsResponse(
    val items: List<CastItemResponse>,
    val job: String
)