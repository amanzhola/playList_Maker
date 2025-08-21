package com.example.playlistmaker.presentation.movieViewModels.movieNames

import android.annotation.SuppressLint
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.playlistmaker.R
import com.example.playlistmaker.domain.api.moviePersons.NamesInteractor
import com.example.playlistmaker.utils.SEARCH_DEBOUNCE_DELAY
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn

@SuppressLint("StaticFieldLeak")
class NamesViewModel(
    private val context: Context,
    private val namesInteractor: NamesInteractor
) : ViewModel() {

    // 🎹 Ввод пользователя (сырой текст)
    private val searchQueryFlow = MutableStateFlow("")

    // 🔔 "одноразовые" события (тосты/снэкбары) — замена SingleLiveEvent
    private val _toasts = MutableSharedFlow<String?>(extraBufferCapacity = 1, replay = 0)
    val toasts = _toasts.asSharedFlow()

    // 🧱 UI-состояние списка имён
    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    val state: StateFlow<NamesState> =
        searchQueryFlow
            .debounce(SEARCH_DEBOUNCE_DELAY)     // ⏳ ждём «тишину» перед поиском
            .map { it.trim(' ', '\t', '\n', '\r') } // ✂️ безопасный trim (эмодзи не ломаем)
            .distinctUntilChanged()
            .flatMapLatest { query ->
                if (query.isBlank()) {
                    // 💤 пустой ввод — пустой контент без «ничего не найдено»
                    flowOf(NamesState.Content(emptyList()))
                } else {
                    // 🔎 реальный запрос
                    namesInteractor.searchNames(query)
                        .map { (foundNames, errorMessage) ->
                            if (errorMessage != null) {
                                _toasts.tryEmit(errorMessage) // 🔔 "техническая" ошибка в тост
                                NamesState.Error(
                                    message = context.getString(R.string.something_went_wrong)
                                )
                            } else {
                                val persons = foundNames.orEmpty()
                                if (persons.isEmpty()) {
                                    NamesState.Empty(
                                        message = context.getString(R.string.nothing_found)
                                    )
                                } else {
                                    NamesState.Content(persons)
                                }
                            }
                        }
                        .onStart { emit(NamesState.Loading) } // ⏳ показываем лоадер только на непустом запросе
                }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.Eagerly,
                // 🧼 стартовое состояние — пустой контент (без "ничего не найдено")
                initialValue = NamesState.Content(emptyList())
            )

    // 📨 Публичный API: прокинуть новый текст (UI-слой дергает это из TextWatcher)
    fun searchDebounce(changedText: String) {
        searchQueryFlow.value = changedText
    }
}
