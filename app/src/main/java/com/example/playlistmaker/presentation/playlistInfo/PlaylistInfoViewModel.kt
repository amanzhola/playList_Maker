package com.example.playlistmaker.presentation.playlistInfo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.playlistmaker.domain.models.search.Track
import com.example.playlistmaker.domain.repository.playlist.PlaylistRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class PlaylistInfoViewModel(
    private val repository: PlaylistRepository
) : ViewModel() {

    data class Ui(
        val name: String,
        val description: String?,
        val coverPath: String?,
        val minutesTotal: Long,   // минуты (сырые)
        val hasCover: Boolean,
        val tracks: List<Track>   // список треков для UI/адаптера
    ){
        val tracksCount: Int get() = tracks.size   // ← вычисляемое свойство
    }

    data class TracksBundle(
        val tracks: List<Track>,
        val minutesTotal: Long
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    fun uiState(playlistId: Long): Flow<Ui> {

        // tracksFlow: поток ТОЛЬКО треков (минуты считаются поверх него в minutesFlow)
        val playlistFlow = repository.observePlaylist(playlistId)

        // ids → треки (в нужном порядке) → сразу считаем минуты
        val tracksBundleFlow: Flow<TracksBundle> = playlistFlow
            .map { it.trackIds }                // ← уже есть в доменной модели
            .distinctUntilChanged()
            .flatMapLatest { ids ->
                if (ids.isEmpty()) flowOf(emptyList())
                else repository.observeTracksByIds(ids)
            }
            .map { tracks ->
                // считаем только когда меняются треки
                val minutes = tracks.asSequence().sumOf { it.trackTimeMillis } / 60_000L
                TracksBundle(tracks = tracks, minutesTotal = minutes)
            }

        return combine(playlistFlow, tracksBundleFlow) { pl, bundle ->
            Ui(
                name = pl.name,
                description = pl.description,
                coverPath = pl.coverPath,
                minutesTotal = bundle.minutesTotal,             // ← берём готовое
                hasCover = !pl.coverPath.isNullOrBlank(),
                tracks = bundle.tracks                    // ← и треки
            )
        }
    }

    fun removeTrack(playlistId: Long, trackId: Int) {
        viewModelScope.launch {
            repository.removeTrackFromPlaylist(playlistId, trackId)
        }
    }

    suspend fun deletePlaylist(id: Long) {
        repository.deletePlaylist(id)
    }
}
