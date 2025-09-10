package com.example.playlistmaker.presentation.import_album

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.playlistmaker.data.mappers.toDomain
import com.example.playlistmaker.domain.models.search.Track
import com.example.playlistmaker.utils.PlaylistImport
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ImportPreviewViewModel : ViewModel() {

    data class Ui(
        val loading: Boolean = true,
        val error: String? = null,
        val name: String = "",
        val description: String? = null,
        val coverUri: Uri? = null,
        val minutesTotal: Long = 0L,
        val tracks: List<Track> = emptyList()
    ) {
        val tracksCount: Int get() = tracks.size
    }

    private val _state = MutableStateFlow(Ui())
    val state: StateFlow<Ui> = _state.asStateFlow()

    fun load(context: Context, inputUri: Uri) {
        if (!_state.value.loading) return
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true, error = null)
            runCatching { PlaylistImport.parse(context, inputUri) }
                .onSuccess { imported ->
                    val domainTracks = imported.tracks.map { it.toDomain() } //  mapper
                    val minutes = domainTracks.sumOf { it.trackTimeMillis } / 60_000L
                    _state.value = Ui(
                        loading = false,
                        name = imported.name,
                        description = imported.description,
                        coverUri = imported.coverUri,
                        minutesTotal = minutes,
                        tracks = domainTracks
                    )
                }
                .onFailure { e ->
                    _state.value = Ui(loading = false, error = e.message ?: "Ошибка импорта")
                }
        }
    }
}
