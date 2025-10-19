package com.example.playlistmaker.data.repository.search

import com.example.playlistmaker.data.song_db.FavoriteTrackDao
import com.example.playlistmaker.domain.api.search.AudioNetworkClient
import com.example.playlistmaker.domain.api.search.AudioRepository
import com.example.playlistmaker.domain.models.search.Track
import com.example.playlistmaker.domain.util.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class AudioRepositoryImpl(
    private val networkClient: AudioNetworkClient,
    private val favoriteTrackDao: FavoriteTrackDao
) : AudioRepository {

    override fun searchTracks(term: String): Flow<Resource<List<Track>>> = flow {
        // 1) сетевой вызов
        val response = networkClient.search(term)

        if (response.isSuccessful) {
            // 2) ВАЖНО: body — это СВОЙСТВО (НЕ body())
            //    results может быть null → трактуем как пусто (это не ошибка сети)
            val dtos = response.body?.results.orEmpty()

            // 3) ускоряем contains за счёт Set (и приводим типы при необходимости)
            val favoriteIds: Set<Int> = favoriteTrackDao.getFavoriteTrackIds().toSet()

            // 4) маппинг DTO -> domain + проставление isFavorite (без отдельного прохода)
            val tracks: List<Track> = dtos.map { dto ->
                Track(
                    trackId = dto.trackId,
                    trackName = dto.trackName ?: "Unknown title",
                    artistName = dto.artistName ?: "Unknown artist",
                    trackTimeMillis = dto.trackTimeMillis ?: 0L,
                    artworkUrl100 = dto.artworkUrl100 ?: "",
                    collectionName = dto.collectionName ?: "",
                    releaseDate = dto.releaseDate ?: "",            // можем — парсить в дату позже
                    primaryGenreName = dto.primaryGenreName ?: "",
                    country = dto.country ?: "",
                    previewUrl = dto.previewUrl ?: "",
                    // внутренние поля доменной модели, не из сети:
                    isPlaying = false,
                    playTime = "0:00"
                ).apply {
                    isFavorite = favoriteIds.contains(trackId)      // если DAO даёт Long → contains(trackId.toLong())
                }
            }

            // 5) ВСЕГДА Success при isSuccessful — даже если список пуст → UI покажет "не найдено"
            emit(Resource.Success(tracks))
        } else { // ❌
            // 6) HTTP-ошибка → Resource.Error
            //    У CustomNetworkResponse нет message(), используем code + errorBody
            val msg = buildString {
                append("HTTP ").append(response.code)
                if (!response.errorBody.isNullOrBlank()) {
                    append(": ").append(response.errorBody)
                }
            }
            emit(Resource.Error("Ошибка: $msg"))
        }
    }
        // 7) Любые исключения (I/O, парсинг, БД) → Error
        .catch { e ->
            emit(Resource.Error("Ошибка: ${e.localizedMessage ?: "Неизвестная ошибка"}"))
        }
        // 8) Сеть/БД — на IO
        .flowOn(Dispatchers.IO)
}
