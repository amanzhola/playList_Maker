package com.example.playlistmaker.presentation.movieDetails

import com.example.playlistmaker.domain.models.movieDetails.MovieDetails

sealed interface AboutState {

    data class Content(
        val movie: MovieDetails
    ) : AboutState

    data class Error(
        val message: String
    ) : AboutState

}