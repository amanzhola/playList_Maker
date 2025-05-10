package com.example.playlistmaker.domain.api

import com.example.playlistmaker.data.dto.MovieAdvancedSearchDto
import com.example.playlistmaker.data.dto.MovieSearchDto
import com.example.playlistmaker.data.dto.SearchResponse
import com.example.playlistmaker.data.network.response.CustomNetworkResponse

interface movieNetworkClient {

    suspend fun searchMovies(expression: String): CustomNetworkResponse<SearchResponse<MovieSearchDto>>
    suspend fun getAdvancedSearch(title: String): CustomNetworkResponse<SearchResponse<MovieAdvancedSearchDto>>
}
