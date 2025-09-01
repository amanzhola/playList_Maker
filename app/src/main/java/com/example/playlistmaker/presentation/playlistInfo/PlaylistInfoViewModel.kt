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
        val tracksCount: Int,     // количество треков
        val hasCover: Boolean,
        val tracks: List<Track>   // список треков для UI/адаптера
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    fun uiState(playlistId: Long): Flow<Ui> {
        val playlistFlow = repository.observePlaylist(playlistId)

        val tracksFlow = playlistFlow
            .map { it.trackIds }                // ← уже есть в доменной модели
            .distinctUntilChanged()
            .flatMapLatest { ids ->
                if (ids.isEmpty()) flowOf(emptyList())
                else repository.observeTracksByIds(ids)
            }

        return combine(playlistFlow, tracksFlow) { pl, tracks ->
            val minutes = tracks.sumOf { it.trackTimeMillis } / 60_000L
            Ui(
                name = pl.name,
                description = pl.description,
                coverPath = pl.coverPath,
                minutesTotal = minutes,
                tracksCount = pl.tracksCount,    // можно взять tracks.size
                hasCover = !pl.coverPath.isNullOrBlank(),
                tracks = tracks
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
