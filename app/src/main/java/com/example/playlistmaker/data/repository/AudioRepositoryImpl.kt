package com.example.playlistmaker.data.repository

import com.example.playlistmaker.domain.api.AudioNetworkClient
import com.example.playlistmaker.domain.api.AudioRepository
import com.example.playlistmaker.domain.models.Track
import com.example.playlistmaker.domain.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow

class AudioRepositoryImpl(
    private val networkClient: AudioNetworkClient
) : AudioRepository {

    override fun searchTracks(term: String): Flow<Resource<List<Track>>> = flow {
        val response = networkClient.search(term)

        if (response.isSuccessful && response.body?.results != null) {
            val tracks = response.body.results.map { dto ->
                Track(
                    trackName = dto.trackName,
                    artistName = dto.artistName,
                    trackTimeMillis = dto.trackTimeMillis,
                    artworkUrl100 = dto.artworkUrl100,
                    collectionName = dto.collectionName,
                    releaseDate = dto.releaseDate,
                    primaryGenreName = dto.primaryGenreName,
                    country = dto.country,
                    previewUrl = dto.previewUrl,
                    trackId = dto.trackId,
                    isPlaying = dto.isPlaying,
                    playTime = dto.playTime
                )
            }
            emit(Resource.Success(tracks))
        } else {
            emit(Resource.Error("Ошибка: ${response.errorBody ?: "Пустой ответ"}"))
        }
    }.catch { e ->
        emit(Resource.Error("Ошибка: ${e.localizedMessage ?: "Неизвестная ошибка"}"))
    }

}

//package com.example.playlistmaker.data.repository
//
//import com.example.playlistmaker.data.network.ITunesApi
//import com.example.playlistmaker.domain.api.AudioRepository
//import com.example.playlistmaker.domain.models.Track
//import com.example.playlistmaker.domain.util.Resource
//import kotlinx.coroutines.flow.Flow
//import kotlinx.coroutines.flow.flow
//
//class AudioRepositoryImpl(private val api: ITunesApi) : AudioRepository {
//    // 🚀
//    override fun searchTracks(term: String): Flow<Resource<List<Track>>> = flow {
//
//        try {
//            val response = api.search(term) // 📡
//
//            if (response.isSuccessful) {
//                val body = response.body()
//                if (body?.results != null) { // ✅
//
//                    val tracks = body.results.map { dto ->
//                        Track(
//                            trackName = dto.trackName,
//                            artistName = dto.artistName,
//                            trackTimeMillis = dto.trackTimeMillis,
//                            artworkUrl100 = dto.artworkUrl100,
//                            collectionName = dto.collectionName,
//                            releaseDate = dto.releaseDate,
//                            primaryGenreName = dto.primaryGenreName,
//                            country = dto.country,
//                            previewUrl = dto.previewUrl,
//                            trackId = dto.trackId,
//                            isPlaying = dto.isPlaying,
//                            playTime = dto.playTime
//                        )
//                    }
//                    emit(Resource.Success(tracks))
//                } else { // ❌
//                    emit(Resource.Error("Нет результатов"))
//                }
//            } else { // ❌
//                emit(Resource.Error("Ошибка сервера: ${response.code()}"))
//            }
//        } catch (e: Exception) { // 💥
//            emit(Resource.Error(e.localizedMessage ?: "Неизвестная ошибка"))
//        }
//    }
//}
