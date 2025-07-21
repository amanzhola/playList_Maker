package com.example.playlistmaker.domain.impl.moviePersons

import com.example.playlistmaker.domain.api.moviePersons.NamesInteractor
import com.example.playlistmaker.domain.api.moviePersons.NamesRepository
import com.example.playlistmaker.domain.models.moviePerson.Person
import com.example.playlistmaker.domain.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class NamesInteractorImpl(private val repository: NamesRepository) : NamesInteractor {

    override fun searchNames(expression: String): Flow<Pair<List<Person>?, String?>> {
        return repository.searchNames(expression).map { result ->
            when(result) {
                is Resource.Success -> {
                    Pair(result.data, null)
                }
                is Resource.Error -> {
                    Pair(null, result.message)
                }
            }
        }
    }
}
