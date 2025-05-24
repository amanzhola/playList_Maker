package com.example.playlistmaker.data.network.movie

import com.example.playlistmaker.data.dto.movie.MovieAdvancedSearchDto
import com.example.playlistmaker.data.dto.movie.MovieSearchDto
import com.example.playlistmaker.data.dto.SearchResponse
import com.example.playlistmaker.data.network.response.CustomNetworkResponse
import com.example.playlistmaker.data.network.safeApiCallAndAdapt
import com.example.playlistmaker.domain.api.movie.movieNetworkClient

class RetrofitMovieNetworkClient(
    private val apiKey: String,
    private val imdbService: IMDbApi
) : movieNetworkClient {

    override suspend fun searchMovies(expression: String): CustomNetworkResponse<SearchResponse<MovieSearchDto>> {
        return safeApiCallAndAdapt { imdbService.searchMovies(apiKey, expression) }
    }

    override suspend fun getAdvancedSearch(title: String): CustomNetworkResponse<SearchResponse<MovieAdvancedSearchDto>> {
        return safeApiCallAndAdapt { imdbService.getAdvancedSearch(apiKey, title) }
    }
}