package com.example.playlistmaker.data.dto.moviePersons

import com.example.playlistmaker.data.dto.movieDetails.Response

class NamesSearchResponse(val searchType: String,
                          val expression: String,
                          val results: List<PersonDto>) : Response()