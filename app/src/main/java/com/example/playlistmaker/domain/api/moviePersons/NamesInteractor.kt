package com.example.playlistmaker.domain.api.moviePersons

import com.example.playlistmaker.domain.models.moviePerson.Person

interface NamesInteractor {

    fun searchNames(expression: String, consumer: NamesConsumer)

    interface NamesConsumer {
        fun consume(foundNames: List<Person>?, errorMessage: String?)
    }
}