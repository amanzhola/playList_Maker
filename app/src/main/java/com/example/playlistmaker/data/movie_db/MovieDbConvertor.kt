package com.example.playlistmaker.data.movie_db

import com.example.playlistmaker.domain.models.movie.Movie

class MovieDbConvertor {

    fun map(movie: Movie): MovieEntity {
        return MovieEntity(movie.id, movie.resultType, movie.image, movie.title,
            movie.description ?: "")
    }

    fun map(movie: MovieEntity): Movie {
        return Movie(movie.id, movie.resultType, movie.image, movie.title, movie.description,
            null, null, null, null, null)
    }
}