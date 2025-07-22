package com.example.playlistmaker.domain.api.moviePersons

import com.example.playlistmaker.domain.models.moviePerson.Person
import com.example.playlistmaker.domain.util.Resource
import kotlinx.coroutines.flow.Flow

interface NamesRepository {
    fun searchNames(expression: String): Flow<Resource<List<Person>>>
}
