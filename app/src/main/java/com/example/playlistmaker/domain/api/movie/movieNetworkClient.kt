package com.example.playlistmaker.domain.api.movie

import com.example.playlistmaker.data.dto.movie.MovieAdvancedSearchDto
import com.example.playlistmaker.data.dto.movie.MovieSearchDto
import com.example.playlistmaker.data.dto.SearchResponse
import com.example.playlistmaker.data.network.response.CustomNetworkResponse

interface movieNetworkClient {

    suspend fun searchMovies(expression: String): CustomNetworkResponse<SearchResponse<MovieSearchDto>>
    suspend fun getAdvancedSearch(title: String): CustomNetworkResponse<SearchResponse<MovieAdvancedSearchDto>>
}
