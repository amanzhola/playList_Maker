package com.example.playlistmaker.domain.api.moviePersons

import com.example.playlistmaker.domain.models.moviePerson.Person
import com.example.playlistmaker.domain.util.Resource


interface NamesRepository {

    fun searchNames(expression: String): Resource<List<Person>>
}