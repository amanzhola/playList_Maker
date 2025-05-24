package com.example.playlistmaker.data.repository.base

import com.example.playlistmaker.domain.api.movie.MovieSerializer
import com.example.playlistmaker.domain.models.movie.Movie
import com.google.gson.Gson

class GsonMovieSerializer(private val gson: Gson) : MovieSerializer {

    override fun serialize(movie: Movie): String = gson.toJson(movie)

    override fun deserialize(json: String): Movie? {
        return try {
            gson.fromJson(json, Movie::class.java)
        } catch (e: Exception) {
            null
        }
    }

    override fun serializeList(movies: List<Movie>): String = gson.toJson(movies)

    override fun deserializeList(json: String): List<Movie> {
        val type = object : com.google.gson.reflect.TypeToken<List<Movie>>() {}.type
        return gson.fromJson(json, type)
    }
}
