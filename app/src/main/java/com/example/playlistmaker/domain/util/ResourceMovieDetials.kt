package com.example.playlistmaker.domain.util

sealed class ResourceMovieDetials<T> {
    class Success<T>(val data: T) : ResourceMovieDetials<T>()
    class Error<T>(val message: String, val data: T? = null) : ResourceMovieDetials<T>()
}
