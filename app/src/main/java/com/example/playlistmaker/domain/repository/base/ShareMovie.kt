package com.example.playlistmaker.domain.repository.base

import com.example.playlistmaker.domain.models.movie.Movie

interface ShareMovie {
    fun shareMovieOrNotify(movie: Movie?)
}