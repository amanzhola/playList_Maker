package com.example.playlistmaker.domain.api.movie

import com.example.playlistmaker.domain.models.movie.Movie

interface MovieSerializer {
    fun serialize(movie: Movie): String
    fun deserialize(json: String): Movie?
    fun serializeList(movies: List<Movie>): String
    fun deserializeList(json: String): List<Movie>?
}
