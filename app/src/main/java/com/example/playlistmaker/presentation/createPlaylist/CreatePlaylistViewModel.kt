package com.example.playlistmaker.presentation.createPlaylist

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.playlistmaker.domain.usecases.createPlaylist.CreatePlaylistUseCase
import com.example.playlistmaker.domain.usecases.createPlaylist.UpdatePlaylistUseCase
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
    // ★ добавляем use case на обновление
    private val updatePlaylist: UpdatePlaylistUseCase
) : ViewModel() {

    // ★ режим работы VM
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

    // Одноразовые события: успех / ошибка
    sealed interface Event {
        data class Saved(val id: Long, val name: String) : Event
        data class Error(val message: String) : Event
    }
    private val _events = Channel<Event>(capacity = Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    // ── ★ Инициализация режима редактирования (вызывается из Fragment один раз) ──
    fun enterEditModeIfNeeded(id: Long, name: String, desc: String?, coverPathOrUri: String?) {
        if (editInitialized) return
        editInitialized = true
        mode = Mode.EDIT
        editId = id
        originalCoverString = coverPathOrUri

        // предзаполняем состояние
        val cover = coverPathOrUri?.let(Uri::parse)
        _state.value = UiState(
            name = name,
            desc = desc.orEmpty(),
            coverUri = cover,
            createEnabled = name.isNotBlank(),
            dirty = false // при входе изменений ещё не было
        )
    }

    // ── Intent’ы ввода ─────────────────────────────────────────────────────────

    // Единая точка изменения стейта: после мутации ВСЕГДА пересчитываем createEnabled
    private fun reduce(mutator: (UiState) -> UiState) {
        val next = mutator(_state.value)
        _state.value = next.copy(createEnabled = next.name.isNotBlank())
    }

    fun onNameChanged(raw: String) = reduce { st ->
        val oneLine = raw.replace("\r", " ").replace("\n", " ").trim()
        st.copy(
            name = oneLine,
            dirty = st.dirty || oneLine.isNotBlank()
        )
    }

    fun onDescChanged(s: String) = reduce { st ->
        st.copy(desc = s, dirty = st.dirty || s.isNotBlank())
    }

    fun onCoverPicked(uri: Uri?) = reduce { st ->
        // add for edit
        val changed = when (mode) {
            Mode.CREATE -> (uri != null)
            Mode.EDIT   -> (uri?.toString() != originalCoverString) // изменили/сбросили
        }

        st.copy(coverUri = uri, dirty = st.dirty || (uri != null))
    }

    fun hasUnsavedChanges(): Boolean = _state.value.dirty

    // ── Сохранение (шаг 7) ─────────────────────────────────────────────────────
    fun save(appContext: Context) {
        val snapshot = _state.value

        if (snapshot.name.isBlank()) {
            // На всякий случай — защита на уровне VM (кнопка-то и так disabled)
            viewModelScope.launch { _events.send(Event.Error("Введите название плейлиста")) }
            return
        }

        viewModelScope.launch {
            try {

                when(mode) {

                    Mode.CREATE -> {
                        // используем опцию 1 избежать дублирования в текущих папках файлав изображения

                        // 1️⃣ 1) Копируем обложку в приватное хранилище (если выбрана)
                        val coverPath: String ? = if (snapshot.coverUri != null) {

                        // 1️⃣ 2) Сохраняем обложку в ОБЩЕЕ хранилище и берём content:// Uri
//                      val coverUriString: String? = if (snapshot.coverUri != null) {

                        withContext(Dispatchers.IO) {
                            // 2️⃣ 1) Копируем обложку в приватное хранилище (если выбрана)
                            FileCopier_private.copyToAppStorage(appContext, snapshot.coverUri)

                            // 2️⃣ 2) Сохраняем обложку в ОБЩЕЕ хранилище и берём content:// Uri
//                        MediaStoreSaver_public.saveCoverToPublicMedia(appContext, snapshot.coverUri)
                        }.also { copied ->

                            // Требование 9: если не скопировалось — не сохраняем, чтобы не потерять обложку
                            if (copied == null) {
                                _events.send(Event.Error("Не удалось сохранить обложку"))
                                return@launch
                            }
                        } // 2️⃣ 🅰️ to save in private storage
//                      } ?.toString() //  2️⃣ 🅱️ to save in public storage
                    } else null

                    //  3️⃣ 2) Пишем в БД (private)
                    //  3️⃣ 2) Пишем в БД (public: меняем семантику параметра: теперь это строка-URI)
                        val id = withContext(Dispatchers.IO) {

                        createPlaylist(
                            snapshot.name.trim(),
                            snapshot.desc.ifBlank { null },
                            coverPath // 3️⃣ file path -> private storage
//                        coverUriString // 3️⃣ для public (private был file path) теперь content Uri (String)
                        )
                    }

//                  // 3) Сообщаем об успехе (фрагмент закроет экран и отправит имя назад для Snackbar)
//                  _events.send(Event.Saved(id = id, name = snapshot.name.trim()))

                    // 3️⃣ 🅰️ (Опционально) Сбросим dirty, если останемся на экране
                    _state.value = _state.value.copy(dirty = false)

                            // 3️⃣ 🅱️ (Опционально) Сбросим dirty, если останемся на экране
//                  _state.value = _state.value.copy(dirty = false,
//                    coverUri = coverUriString?.let(Uri::parse) ?: snapshot.coverUri)

                            // 3) Сообщаем об успехе (фрагмент закроет экран и отправит имя назад для Snackbar)
                            _events . send (Event.Saved(id = id, name = snapshot.name.trim()))
                    }

                    Mode.EDIT -> {
                        // === Новая ветка ОБНОВЛЕНИЯ ===
                        val currentCover = snapshot.coverUri?.toString()
                        val coverChanged = currentCover != originalCoverString

                        val newCoverPath: String? = if (coverChanged) {
                            // если обложку очистили → null; если поставили новую → копируем
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
                            // не меняли — оставляем прежнюю
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
}

//*************************************************************************
/* лучшим оказалось хранить в private чтобы чище было в папках хранения
//+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
Option A private storage share:
//+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

1. Считаем из coverPath из БД и получим Uri
val file = File(coverPath)
val uri = FileProvider.getUriForFile(
    context,
    "${context.packageName}.fileprovider",
    file
)

2. Соберем интент:
val share = Intent(Intent.ACTION_SEND).apply {
    type = "image/jpeg"
    putExtra(Intent.EXTRA_STREAM, uri)
    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
}
context.startActivity(Intent.createChooser(share, "Поделиться плейлистом"))

3. Добавить в манифест <provider> и file_paths.xml.

//+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
Option B publick storage share:
//+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

// Раз храним в content://-URI, просто передайте его в EXTRA_STREAM: (FileProvider не нужен.)

val cover = uriStringFromDb?.let(Uri::parse)
val share = Intent(Intent.ACTION_SEND).apply {
    type = "image/jpeg"
    putExtra(Intent.EXTRA_STREAM, cover)
    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    // на всякий случай — корректная раздача доступа получателю
    clipData = ClipData.newUri(context.contentResolver, "cover", cover)
}
startActivity(Intent.createChooser(share, "Поделиться плейлистом"))

*/