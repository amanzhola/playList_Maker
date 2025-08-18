package com.example.playlistmaker.presentation.createPlaylist

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.playlistmaker.domain.usecases.createPlaylist.CreatePlaylistUseCase
import com.example.playlistmaker.presentation.FileCopier
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CreatePlaylistViewModel(
    private val createPlaylist: CreatePlaylistUseCase
) : ViewModel() {

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
                // 1) Копируем обложку в приватное хранилище (если выбрана)
                val coverPath: String? = if (snapshot.coverUri != null) {
                    withContext(Dispatchers.IO) {
                        FileCopier.copyToAppStorage(appContext, snapshot.coverUri)
                    }.also { copied ->

                        // Требование 9: если не скопировалось — не сохраняем, чтобы не потерять обложку
                        if (copied == null) {
                            _events.send(Event.Error("Не удалось сохранить обложку"))
                            return@launch
                        }
                    }
                } else null

                // 2) Пишем в БД
                val id = withContext(Dispatchers.IO) {

                    createPlaylist(
                        snapshot.name.trim(),
                        snapshot.desc.ifBlank { null },
                        coverPath
                    )
                }

                // 3) Сообщаем об успехе (фрагмент закроет экран и отправит имя назад для Snackbar)
                _events.send(Event.Saved(id = id, name = snapshot.name.trim()))

                // (Опционально) Сбросим dirty, если останемся на экране
                _state.value = _state.value.copy(dirty = false)

            } catch (e: Exception) {
                _events.send(Event.Error(e.message ?: "Ошибка сохранения плейлиста"))
            }
        }
    }
}
