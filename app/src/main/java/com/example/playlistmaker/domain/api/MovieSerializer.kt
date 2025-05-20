package com.example.playlistmaker.domain.api

import com.example.playlistmaker.domain.models.Movie

interface MovieSerializer {
    fun serialize(movie: Movie): String
    fun deserialize(json: String): Movie?
    fun serializeList(movies: List<Movie>): String
    fun deserializeList(json: String): List<Movie>?
}
