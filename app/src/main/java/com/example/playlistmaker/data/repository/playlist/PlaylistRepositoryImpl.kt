package com.example.playlistmaker.data.repository.playlist

//import com.google.firebase.crashlytics.buildtools.reloc.com.google.common.reflect.TypeToken
import com.example.playlistmaker.data.createPlaylist.PlaylistDao
import com.example.playlistmaker.data.createPlaylist.PlaylistEntity
import com.example.playlistmaker.data.mappers.toDomain
import com.example.playlistmaker.data.playlist.PlaylistTrackDao
import com.example.playlistmaker.data.playlist.PlaylistTrackEntity
import com.example.playlistmaker.domain.models.playlist.Playlist
import com.example.playlistmaker.domain.models.search.Track
import com.example.playlistmaker.domain.repository.playlist.PlaylistRepository
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flowOf
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
        val type = object : TypeToken<List<Int>>() {}.type
        val current = (gson.fromJson<List<Int>>(pl.trackIdsJson, type) ?: emptyList()).toMutableList()

        if (current.contains(track.trackId)) return false // уже есть

        // 3) ДОБАВЛЯЕМ В КОНЕЦ (а не в начало)
        current.add(track.trackId) // ✅ новые треки будут внизу

        // сериализуем и обновляем count по факту
        val newJson = gson.toJson(current)
        playlistDao.updateTracks(
            id = pl.id,
            trackIdsJson = newJson,
            count = current.size // ✅ надёжнее, чем pl.tracksCount + 1
        )

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

    override suspend fun removeTrackFromPlaylist(playlistId: Long, trackId: Int) {
        val pl = playlistDao.getById(playlistId) ?: return

        // 1) выкидываем id из JSON-списка и уменьшаем счётчик
        val type = object : TypeToken<List<Int>>() {}.type
        val ids = (gson.fromJson<List<Int>>(pl.trackIdsJson, type) ?: emptyList()).toMutableList()
        if (!ids.remove(trackId)) return

        playlistDao.updateTracks(
            id = pl.id,
            trackIdsJson = gson.toJson(ids),
            count = (pl.tracksCount - 1).coerceAtLeast(0)
        )

        // 2) зачистка «пула»: если trackId уже не фигурирует ни в одном плейлисте — удаляем из playlist_tracks
        val anyStillUses = playlistDao.getAll().any { p ->
            val list = gson.fromJson<List<Int>>(p.trackIdsJson, type) ?: emptyList()
            trackId in list
        }
        if (!anyStillUses) {
            playlistTrackDao.deleteById(trackId)
        }
    }

    // исправление падения при уничтожении альбомов также удаляются треки -> отсекаем null-эмиссии
    override fun observePlaylist(id: Long): Flow<Playlist> =
        playlistDao.observeById(id)
            .filterNotNull()              // ← отсекаем null-эмиссии
            .map { e -> e.toDomain(gson) } // .toDomain уже собирает trackIds: List<Int>

    override fun observeTracksByIds(ids: List<Int>): Flow<List<Track>> {
        if (ids.isEmpty()) return flowOf(emptyList())

        // карта «id → позиция» из исходного порядка
        val order = ids.withIndex().associate { (index, id) -> id to index }

        return playlistTrackDao.observeByIds(ids)
            .map { entities ->
                entities
                    .map(PlaylistTrackEntity::toDomain)
                    // ❌ новые сверху
                    .sortedByDescending { order[it.trackId] ?: Int.MAX_VALUE }
            }
    }

    override suspend fun deletePlaylist(playlistId: Long) {
        // 1) найдём плейлист
        val pl = playlistDao.getById(playlistId) ?: return

        // 2) сохраним список его треков
        val type = object : TypeToken<List<Int>>() {}.type
        val myIds = gson.fromJson<List<Int>>(pl.trackIdsJson, type) ?: emptyList()

        // 3) удалим сам плейлист
        playlistDao.deleteById(playlistId)

        // 4) перечитаем ВСЕ плейлисты (без удалённого) и проверим «осиротевшие» треки
        val all = playlistDao.getAll()
        val stillUsed = buildSet {
            all.forEach { other ->
                val ids = gson.fromJson<List<Int>>(other.trackIdsJson, type) ?: emptyList()
                addAll(ids)
            }
        }

        // 5) если какой-то трек из myIds больше нигде не используется — удалим из playlist_tracks
        myIds.forEach { tid ->
            if (tid !in stillUsed) {
                playlistTrackDao.deleteById(tid)
            }
        }
    }

    // CreatePlaylistFragment edit (updating)
    override suspend fun updatePlaylistMetadata(
        id: Long,
        name: String,
        description: String?,
        coverPath: String?
    ) {
        // Вариант А : точечный UPDATE — не трогаем трековые поля
        playlistDao.updateMetadata(id, name, description, coverPath)

        // ───── Вариант B (альтернатива raw-Query):
        // val current = playlistDao.getById(id) ?: return
        // playlistDao.update(
        //     current.copy(
        //         name = name,
        //         description = description,
        //         coverPath = coverPath
        //     )
        // )
    }
}

