package com.example.playlistmaker.presentation.createPlaylist

import android.content.Context
import android.net.Uri
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.playlistmaker.domain.models.search.Track
import com.example.playlistmaker.domain.usecases.createPlaylist.CreatePlaylistUseCase
import com.example.playlistmaker.domain.usecases.createPlaylist.UpdatePlaylistUseCase
import com.example.playlistmaker.domain.usecases.playlist.AddTrackToPlaylistUseCase
import com.example.playlistmaker.presentation.FileCopier_private
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class CreatePlaylistViewModel(
    private val createPlaylist: CreatePlaylistUseCase,
    private val updatePlaylist: UpdatePlaylistUseCase,
    private val addTrackToPlaylist: AddTrackToPlaylistUseCase,
) : ViewModel() {

    private var pendingImportTracks: List<Track> = emptyList()

    private var originalName = ""
    private var originalDesc = ""

    private enum class Mode { CREATE, EDIT }
    private var mode: Mode = Mode.CREATE
    private var editId: Long? = null
    private var originalCoverUri: Uri? = null
    private var editInitialized = false

    data class UiState(
        val name: String = "",
        val desc: String = "",
        val coverUri: Uri? = null,
        val createEnabled: Boolean = false,
        val dirty: Boolean = false,
    )

    private val _state = MutableStateFlow(UiState())
    val state: StateFlow<UiState> = _state.asStateFlow()

    sealed interface Event {
        data class Saved(val id: Long, val name: String) : Event
        data class Error(val message: String) : Event
    }
    private val _events = Channel<Event>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    /** --- Режим редактирования ---  */
    fun enterEditModeIfNeeded(
        id: Long,
        name: String,
        desc: String?,
        coverUri: Uri?,          // ← теперь Uri?, а не String?
    ) {
        if (editInitialized) return
        editInitialized = true

        mode = Mode.EDIT
        editId = id
        originalName = name
        originalDesc = desc.orEmpty()
        originalCoverUri = coverUri

        _state.value = UiState(
            name = name,
            desc = originalDesc,
            coverUri = coverUri,
            createEnabled = name.isNotBlank(),
            dirty = false
        )
    }

    fun onNameChanged(raw: String) = reduce { st ->
        val oneLine = raw.replace("\r", " ").replace("\n", " ")
        val dirty =
            if (mode == Mode.EDIT) computeDirty(oneLine, st.desc, st.coverUri)
            else (st.dirty || oneLine.isNotBlank())
        st.copy(name = oneLine, dirty = dirty)
    }

    fun onDescChanged(s: String) = reduce { st ->
        val dirty =
            if (mode == Mode.EDIT) computeDirty(st.name, s, st.coverUri)
            else (st.dirty || s.isNotBlank())
        st.copy(desc = s, dirty = dirty)
    }

    fun onCoverPicked(uri: Uri?) = reduce { st ->
        val dirty =
            if (mode == Mode.EDIT) computeDirty(st.name, st.desc, uri)
            else (st.dirty || (uri != null))
        st.copy(coverUri = uri, dirty = dirty)
    }

    fun hasUnsavedChanges(): Boolean = _state.value.dirty

    fun save(appContext: Context) {
        val snapshot = _state.value
        if (snapshot.name.isBlank()) {
            viewModelScope.launch { _events.send(Event.Error("Введите название плейлиста")) }
            return
        }

        viewModelScope.launch {
            try {
                when (mode) {
                    Mode.CREATE -> {
                        val coverPath: String? = snapshot.coverUri?.let { src ->
                            withContext(Dispatchers.IO) {
                                FileCopier_private.copyToAppStorage(appContext, src)
                            } ?: run {
                                _events.send(Event.Error("Не удалось сохранить обложку"))
                                return@launch
                            }
                        }
                        val cleanedName = snapshot.name.trimEnd()
                        val newId = withContext(Dispatchers.IO) {
                            createPlaylist(
                                cleanedName,
                                snapshot.desc.ifBlank { null },
                                coverPath
                            )
                        }
                        _events.send(Event.Saved(newId, cleanedName))

                        if (pendingImportTracks.isNotEmpty()) {
                            withContext(Dispatchers.IO) {
                                for (t in pendingImportTracks) {
                                    runCatching { addTrackToPlaylist(newId, t) }
                                }
                            }
                        }

                        _state.value = _state.value.copy(dirty = false)
                        _events.send(Event.Saved(newId, snapshot.name.trim()))
                    }

                    Mode.EDIT -> {
                        val coverChanged = !urisEqual(snapshot.coverUri, originalCoverUri)

                        val newCoverPath: String? = if (coverChanged) {
                            snapshot.coverUri?.let { src ->
                                withContext(Dispatchers.IO) {
                                    FileCopier_private.copyToAppStorage(appContext, src)
                                } ?: run {
                                    _events.send(Event.Error("Не удалось сохранить обложку"))
                                    return@launch
                                }
                            }
                        } else {
                            originalCoverUri?.toString()
                        }

                        val id = requireNotNull(editId)
                        val cleanedEditName = snapshot.name.trimEnd()
                        withContext(Dispatchers.IO) {
                            updatePlaylist(
                                id = id,
                                name = cleanedEditName,
                                desc = snapshot.desc.ifBlank { null },
                                coverPath = newCoverPath
                            )
                        }
                        originalName = cleanedEditName
                        _events.send(Event.Saved(id, cleanedEditName))

                        originalName = snapshot.name.trim()
                        originalDesc = snapshot.desc
                        originalCoverUri = snapshot.coverUri

                        _state.value = _state.value.copy(dirty = false)
                        _events.send(Event.Saved(id, snapshot.name.trim()))
                    }
                }
            } catch (e: Exception) {
                _events.send(Event.Error(e.message ?: "Ошибка сохранения плейлиста"))
            }
        }
    }

    fun prefillForCreate(
        name: String,
        desc: String?,
        cover: String?,
        tracks: List<Track> = emptyList(),
    ) {
        _state.value = _state.value.copy(
            name = name,
            desc = desc.orEmpty(),
            coverUri = cover?.let { toUriOrNull(it) },
            dirty = false,
            createEnabled = name.isNotBlank()
        )
        pendingImportTracks = tracks
    }

    // ---------- Helpers ----------

    private fun computeDirty(nextName: String, nextDesc: String, nextCover: Uri?): Boolean {
        val nameChanged = nextName != originalName
        val descChanged = nextDesc != originalDesc
        val coverChanged = !urisEqual(nextCover, originalCoverUri)
        return nameChanged || descChanged || coverChanged
    }

    private inline fun reduce(mutator: (UiState) -> UiState) {
        val prev = _state.value
        val next = mutator(prev)
        _state.value = next.copy(createEnabled = next.name.isNotBlank())
    }

    private fun toUriOrNull(s: String?): Uri? {
        val str = s?.trim()?.takeIf { it.isNotEmpty() } ?: return null
        return when {
            str.startsWith("content://") ||
                    str.startsWith("file://") ||
                    str.startsWith("http://") ||
                    str.startsWith("https://") -> str.toUri()
            str.startsWith("/") -> File(str).takeIf { it.exists() }?.let { Uri.fromFile(it) }
            else -> null
        }
    }

    private fun urisEqual(a: Uri?, b: Uri?): Boolean =
        (a?.toString() ?: "") == (b?.toString() ?: "")
}
