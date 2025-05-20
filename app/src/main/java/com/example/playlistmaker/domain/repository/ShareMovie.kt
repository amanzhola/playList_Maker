package com.example.playlistmaker.domain.repository

import com.example.playlistmaker.domain.models.Movie

interface ShareMovie {
    fun shareMovieOrNotify(movie: Movie?)
}