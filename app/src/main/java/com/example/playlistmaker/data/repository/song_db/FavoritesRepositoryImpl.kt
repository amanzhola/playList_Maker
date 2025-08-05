package com.example.playlistmaker.data.repository.song_db

import com.example.playlistmaker.data.song_db.FavoriteTrackDao
import com.example.playlistmaker.data.song_db.FavoriteTrackEntity
import com.example.playlistmaker.domain.api.song_db.FavoriteTracksRepository
import com.example.playlistmaker.domain.models.search.Track
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

class FavoritesRepositoryImpl(
    private val dao: FavoriteTrackDao
) : FavoriteTracksRepository {

    override suspend fun addToFavorites(track: Track) {
        dao.addToFavorites(track.toEntity())
    }

    override suspend fun removeFromFavorites(track: Track) {
        dao.removeFromFavorites(track.toEntity())
    }

    override fun getAllFavorites(): Flow<List<Track>> = flow {
        emit(dao.getAllFavorites())
    }.map { list ->
        list.map { it.toDomain() } // ❌ убрали сортировку // уже отсортировано в БД
//        list.map { it.toDomain() }.sortedByDescending { it.trackId }
    }

    // Маппинг: Domain -> Entity
    private fun Track.toEntity(): FavoriteTrackEntity {
        return FavoriteTrackEntity(
            trackId = trackId,
            artworkUrl100 = artworkUrl100,
            trackName = trackName,
            artistName = artistName,
            collectionName = collectionName,
            releaseDate = releaseDate,
            primaryGenreName = primaryGenreName,
            country = country,
            trackTimeMillis = trackTimeMillis,
            previewUrl = previewUrl,
            addedAt = System.currentTimeMillis() // 🆕 текущее время
        )
    }

    // Маппинг: Entity -> Domain
    private fun FavoriteTrackEntity.toDomain(): Track {
        return Track(
            trackId = trackId,
            artworkUrl100 = artworkUrl100,
            trackName = trackName,
            artistName = artistName,
            collectionName = collectionName ?: "",
            releaseDate = releaseDate,
            primaryGenreName = primaryGenreName,
            country = country,
            trackTimeMillis = trackTimeMillis,
            previewUrl = previewUrl,
            isFavorite = true // при загрузке из избранного всегда true
        )
    }
}
