package com.example.playlistmaker.data.dto.movieDetails

class MoviesSearchResponse(val searchType: String,
                           val expression: String,
                           val results: List<MovieDto>) : Response()