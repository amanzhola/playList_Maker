package com.example.playlistmaker.presentation.movieViewModels.movieNames

import com.example.playlistmaker.domain.models.moviePerson.Person

sealed interface NamesState {

    object Loading : NamesState

    data class Content(
        val persons: List<Person>
    ) : NamesState

    data class Error(
        val message: String
    ) : NamesState

    data class Empty(
        val message: String
    ) : NamesState

}