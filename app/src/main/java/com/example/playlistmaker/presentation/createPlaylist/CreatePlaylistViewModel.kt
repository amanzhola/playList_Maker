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

class CreatePlaylistViewModel(
    private val createPlaylist: CreatePlaylistUseCase,
    private val updatePlaylist: UpdatePlaylistUseCase,
    private val addTrackToPlaylist: AddTrackToPlaylistUseCase,
) : ViewModel() {

    // то, что пришло из ImportPreview (или пусто)
    private var pendingImportTracks: List<Track> = emptyList()

    private var originalName: String = ""
    private var originalDesc: String = ""

    private enum class Mode { CREATE, EDIT }
    private var mode: Mode = Mode.CREATE
    private var editId: Long? = null
    private var originalCoverString: String? = null
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
    private val _events = Channel<Event>(capacity = Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    fun enterEditModeIfNeeded(id: Long, name: String, desc: String?, coverPathOrUri: String?) {
        if (editInitialized) return
        editInitialized = true

        mode = Mode.EDIT
        editId = id
        originalName = name
        originalDesc = desc.orEmpty()
        originalCoverString = coverPathOrUri

        _state.value = UiState(
            name = name,
            desc = originalDesc,
            coverUri = toUriOrNull(coverPathOrUri),
            createEnabled = name.isNotBlank(),
            dirty = false
        )
    }

    // --- helpers ---
    private fun computeDirty(nextName: String, nextDesc: String, nextCover: Uri?): Boolean {
        val coverChanged = (nextCover?.toString() ?: "") != (originalCoverString ?: "")
        return nextName != originalName || nextDesc != originalDesc || coverChanged
    }

    private fun reduce(mutator: (UiState) -> UiState) {
        val prev = _state.value
        val next = mutator(prev)
        _state.value = next.copy(createEnabled = next.name.isNotBlank())
    }

    fun onNameChanged(raw: String) = reduce { st ->
        val oneLine = raw.replace("\r", " ").replace("\n", " ").trim()
        val dirty = if (mode == Mode.EDIT)
            computeDirty(oneLine, st.desc, st.coverUri)
        else
            (st.dirty || oneLine.isNotBlank())

        st.copy(name = oneLine, dirty = dirty)
    }

    fun onDescChanged(s: String) = reduce { st ->
        val dirty = if (mode == Mode.EDIT)
            computeDirty(st.name, s, st.coverUri)
        else
            (st.dirty || s.isNotBlank())
        st.copy(desc = s, dirty = dirty)
    }

    fun onCoverPicked(uri: Uri?) = reduce { st ->
        val dirty = if (mode == Mode.EDIT)
            computeDirty(st.name, st.desc, uri)
        else
            (st.dirty || (uri != null))
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

                when(mode) {

                    Mode.CREATE -> {

                        val coverPath: String ? = if (snapshot.coverUri != null) {

                        withContext(Dispatchers.IO) {
                            FileCopier_private.copyToAppStorage(appContext, snapshot.coverUri)

                        }.also { copied ->
                            if (copied == null) {
                                _events.send(Event.Error("Не удалось сохранить обложку"))
                                return@launch
                            }
                        }
                    } else null

                        // 1) создаём плейлист
                        val id = withContext(Dispatchers.IO) {

                            createPlaylist(
                                snapshot.name.trim(),
                                snapshot.desc.ifBlank { null },
                                coverPath
                            )
                        }

                        // 2) добавляем треки, если они пришли из импорта
                        // после получения newId
                        if (pendingImportTracks.isNotEmpty()) {
                            withContext(Dispatchers.IO) {
                                for (t in pendingImportTracks) {
                                    runCatching { addTrackToPlaylist(id, t) }
                                }
                            }
                        }

                        _state.value = _state.value.copy(dirty = false)
                        _events . send (Event.Saved(id = id, name = snapshot.name.trim()))
                    }

                    Mode.EDIT -> {
                        val currentCover = snapshot.coverUri?.toString()
                        val coverChanged = currentCover != originalCoverString

                        val newCoverPath: String? = if (coverChanged) {
                            if (snapshot.coverUri == null) {
                                null
                            } else {
                                withContext(Dispatchers.IO) {
                                    FileCopier_private.copyToAppStorage(appContext, snapshot.coverUri)
                                }.also { copied ->
                                    if (copied == null) {
                                        _events.send(Event.Error("Не удалось сохранить обложку"))
                                        return@launch
                                    }
                                }
                            }
                        } else {
                            originalCoverString
                        }

                        withContext(Dispatchers.IO) {
                            updatePlaylist(
                                id = requireNotNull(editId) { "editId is null in EDIT mode" },
                                name = snapshot.name.trim(),
                                desc = snapshot.desc.ifBlank { null },
                                coverPath = newCoverPath
                            )
                        }
                        _state.value = _state.value.copy(dirty = false)
                        _events.send(Event.Saved(id = requireNotNull(editId), name = snapshot.name.trim()))
                    }
                }
            } catch (e: Exception) {
                _events.send(Event.Error(e.message ?: "Ошибка сохранения плейлиста"))
            }
        }
    }

    private fun toUriOrNull(s: String?): Uri? {
        val str = s?.trim()?.takeIf { it.isNotEmpty() } ?: return null

        return when {
            str.startsWith("content://") ||
                    str.startsWith("file://")    ||
                    str.startsWith("http://")    ||
                    str.startsWith("https://")   -> str.toUri()

            str.startsWith("/") -> Uri.fromFile(java.io.File(str)) // file path
            else -> null
        }
    }

    // import
    fun prefillForCreate(
        name: String,
        desc: String?,
        cover: String?,
        tracks: List<Track> = emptyList()   // ← опционально
    ) {
        // режим CREATE: просто кладём значения в state; dirty = false (пользователь ещё не трогал)
        _state.value = _state.value.copy(
            name = name,
            desc = desc.orEmpty(),
            coverUri = toUriOrNull(cover),
            dirty = false,
            createEnabled = name.isNotBlank()
        )
        pendingImportTracks = tracks // добавлен для кол-во треков для инфо при сохранении альбома
    }
}
