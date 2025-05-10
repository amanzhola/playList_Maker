package com.example.playlistmaker.data.network

import com.example.playlistmaker.data.dto.MovieAdvancedSearchDto
import com.example.playlistmaker.data.dto.MovieSearchDto
import com.example.playlistmaker.data.dto.SearchResponse
import com.example.playlistmaker.data.network.response.CustomNetworkResponse
import com.example.playlistmaker.domain.api.movieNetworkClient

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