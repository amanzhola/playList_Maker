package com.example.playlistmaker.data.repository.moviePersons

import com.example.playlistmaker.data.dto.moviePersons.NamesSearchRequest
import com.example.playlistmaker.data.dto.moviePersons.NamesSearchResponse
import com.example.playlistmaker.data.network.movieDetails.NetworkClient
import com.example.playlistmaker.domain.api.moviePersons.NamesRepository
import com.example.playlistmaker.domain.models.moviePerson.Person
import com.example.playlistmaker.domain.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class NamesRepositoryImpl(private val networkClient: NetworkClient) : NamesRepository {

    override fun searchNames(expression: String): Flow<Resource<List<Person>>> = flow {
        val response = networkClient.doRequest(NamesSearchRequest(expression))
        when (response.resultCode) {
            -1 -> {
                emit(Resource.Error("Проверьте подключение к интернету"))
            }
            200 -> {
                with(response as NamesSearchResponse) {
                    val data = results.map {
                        Person(id = it.id,
                            name = it.title,
                            description = it.description,
                            photoUrl = it.image)
                    }
                    emit(Resource.Success(data))
                }
            }
            else -> {
                emit(Resource.Error("Ошибка сервера"))
            }
        }
    }
}
