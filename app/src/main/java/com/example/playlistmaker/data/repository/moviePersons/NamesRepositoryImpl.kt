package com.example.playlistmaker.data.repository.moviePersons

import com.example.playlistmaker.data.dto.moviePersons.NamesSearchRequest
import com.example.playlistmaker.data.dto.moviePersons.NamesSearchResponse
import com.example.playlistmaker.data.network.movieDetails.NetworkClient
import com.example.playlistmaker.domain.api.moviePersons.NamesRepository
import com.example.playlistmaker.domain.models.moviePerson.Person
import com.example.playlistmaker.domain.util.Resource

class NamesRepositoryImpl(private val networkClient: NetworkClient) : NamesRepository {

    override fun searchNames(expression: String): Resource<List<Person>> {
        val response = networkClient.doRequest(NamesSearchRequest(expression))
        return when (response.resultCode) {
            -1 -> {
                Resource.Error("Проверьте подключение к интернету")
            }
            200 -> {
                with(response as NamesSearchResponse) {
                    Resource.Success(results.map {
                        Person(id = it.id,
                            name = it.title,
                            description = it.description,
                            photoUrl = it.image)
                    })
                }
            }
            else -> {
                Resource.Error("Ошибка сервера")
            }
        }
    }
}