package com.example.playlistmaker.domain.api.moviePersons

import com.example.playlistmaker.domain.models.moviePerson.Person
import kotlinx.coroutines.flow.Flow

interface NamesInteractor {
    fun searchNames(expression: String): Flow<Pair<List<Person>?, String?>>
}
