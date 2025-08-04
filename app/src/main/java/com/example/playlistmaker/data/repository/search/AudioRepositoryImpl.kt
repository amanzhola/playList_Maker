package com.example.playlistmaker.data.repository.search

import com.example.playlistmaker.data.song_db.FavoriteTrackDao
import com.example.playlistmaker.domain.api.search.AudioNetworkClient
import com.example.playlistmaker.domain.api.search.AudioRepository
import com.example.playlistmaker.domain.models.search.Track
import com.example.playlistmaker.domain.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow

class AudioRepositoryImpl(
    private val networkClient: AudioNetworkClient,
    private val favoriteTrackDao: FavoriteTrackDao // 🆕
) : AudioRepository { // 📡

    override fun searchTracks(term: String): Flow<Resource<List<Track>>> = flow {
        val response = networkClient.search(term)

        if (response.isSuccessful && response.body?.results != null) {
            val tracks = response.body.results.map { dto -> // ✅
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

            // 🆕 Получаем ID избранных треков из базы
            val favoriteIds = favoriteTrackDao.getFavoriteTrackIds()

            // 🆕 Проставляем isFavorite для избранных
            tracks.forEach { track ->
                if (favoriteIds.contains(track.trackId)) {
                    track.isFavorite = true
                }
            }

            emit(Resource.Success(tracks))
        } else { // ❌
            emit(Resource.Error("Ошибка: ${response.errorBody ?: "Пустой ответ"}"))
        }
    }.catch { e -> // ❌
        emit(Resource.Error("Ошибка: ${e.localizedMessage ?: "Неизвестная ошибка"}"))
    }

}
