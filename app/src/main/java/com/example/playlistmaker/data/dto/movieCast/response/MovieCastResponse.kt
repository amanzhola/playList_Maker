package com.example.playlistmaker.data.dto.movieCast.response

import com.example.playlistmaker.data.dto.movieDetails.Response

data class MovieCastResponse(
    val actors: List<ActorResponse>,
    val directors: DirectorsResponse,
    val errorMessage: String,
    val fullTitle: String,
    val imDbId: String,
    val others: List<OtherResponse>,
    val title: String,
    val type: String,
    val writers: WritersResponse,
    val year: String
): Response()