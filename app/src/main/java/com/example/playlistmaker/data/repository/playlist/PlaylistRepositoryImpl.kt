package com.example.playlistmaker.data.repository.playlist

import com.example.playlistmaker.data.createPlaylist.PlaylistDao
import com.example.playlistmaker.data.createPlaylist.PlaylistEntity
import com.example.playlistmaker.data.mappers.toDomain
import com.example.playlistmaker.data.playlist.PlaylistTrackDao
import com.example.playlistmaker.data.playlist.PlaylistTrackEntity
import com.example.playlistmaker.domain.models.playlist.Playlist
import com.example.playlistmaker.domain.models.search.Track
import com.example.playlistmaker.domain.repository.playlist.PlaylistRepository
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class PlaylistRepositoryImpl(
    private val playlistDao: PlaylistDao,
    private val playlistTrackDao: PlaylistTrackDao,
    private val gson: Gson
) : PlaylistRepository {

    override suspend fun create(name: String, description: String?, coverPath: String?): Long {

        return playlistDao.insert(
            PlaylistEntity(
                name = name,
                description = description,
                coverPath = coverPath,
                trackIdsJson = "[]",
                tracksCount = 0
            )
        )
    }

    override fun observeAll(): Flow<List<Playlist>> =
        playlistDao.observeAll().map { it.toDomain(gson) }

    // НОВОЕ: добавление трека в плейлист. Возвращает true, если реально добавили.
    override suspend fun addTrackToPlaylist(playlistId: Long, track: Track): Boolean {
        // 1) Читаем плейлист
        val pl = playlistDao.getById(playlistId) ?: return false

        // 2) Парсим IDs
        val type = object : com.google.gson.reflect.TypeToken<List<Int>>() {}.type
        val current = (gson.fromJson<List<Int>>(pl.trackIdsJson, type) ?: emptyList()).toMutableList()

        if (current.contains(track.trackId)) return false // уже есть

        // 3) Обновляем список и счётчик
        current.add(0, track.trackId)
        val newJson = gson.toJson(current)
        playlistDao.updateTracks(pl.id, newJson, pl.tracksCount + 1)

        // 4) Кладём сам трек в «пул» треков плейлистов (IGNORE — защитит от дублей)
        playlistTrackDao.insertIgnore(
            PlaylistTrackEntity(
                trackId = track.trackId,
                artworkUrl100 = track.artworkUrl100,
                trackName = track.trackName,
                artistName = track.artistName,
                collectionName = track.collectionName,
                releaseDate = track.releaseDate,
                primaryGenreName = track.primaryGenreName,
                country = track.country,
                trackTimeMillis = track.trackTimeMillis,
                previewUrl = track.previewUrl
            )
        )
        return true
    }
}

