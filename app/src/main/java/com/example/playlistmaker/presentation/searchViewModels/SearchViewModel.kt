package com.example.playlistmaker.presentation.searchViewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.playlistmaker.domain.api.base.SearchHistoryInteraction
import com.example.playlistmaker.domain.api.search.AudioInteraction
import com.example.playlistmaker.domain.models.search.Track
import com.example.playlistmaker.domain.util.Resource
import com.example.playlistmaker.presentation.searchViewModels.models.SearchUiState
import com.example.playlistmaker.utils.SEARCH_DEBOUNCE_DELAY
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class ErrorState { ERROR, FAILURE, NONE }

class SearchViewModel(
    private val audioInteraction: AudioInteraction,
    private val searchHistoryInteraction: SearchHistoryInteraction
) : ViewModel() {

    private val _openTrack = MutableSharedFlow<Track>(extraBufferCapacity = 1)
    val openTrack: SharedFlow<Track> = _openTrack

<<<<<<< Updated upstream
=======
    private val listItemBackgroundColorFlow = MutableStateFlow<Int?>(null)
    private val listTextColorFlow = MutableStateFlow<Int?>(null)
    private val listArrowColorFlow = MutableStateFlow<Int?>(null)

    // 1) Локальный аккумулятор для цепочки combine
    private data class Acc(
        val p: P1,
        val execQ: String,
        val removedIds: Set<Int>,
        val listTextColor: Int?,      // может быть null
        val listArrowColor: Int?      // может быть null
    )

>>>>>>> Stashed changes
    // 🧩 ввод и фокус
    private val queryFlow = MutableStateFlow("")
    private val focusFlow = MutableStateFlow(false)

    // 🙋 ручной "поиск по Done" (если понадобится)
    private val manualSearchRequests = MutableSharedFlow<String>(extraBufferCapacity = 1)

    // 💾 последний подтверждённый запрос (после дебаунса ИЛИ ручного запуска)
    private val lastSubmittedQuery = MutableStateFlow("")

    // ⛔ локально скрытые треки из текущей выдачи
    private val removedFromSearch = MutableStateFlow<Set<Int>>(emptySet())

    // 🕘 История: сначала снимок, затем “живая” подписка
    private val historyFlow: Flow<List<Track>> =
        searchHistoryInteraction
            .observeHistory()
            .onStart { emit(searchHistoryInteraction.getHistory()) }

    // 🔤 без поломки emoji — режем только обычные пробелы/переносы
    private fun String.trimForEmoji(): String = trim(' ', '\t', '\n', '\r')

    // ✅ ЕДИНАЯ нормализация строки запроса
    // -----------------------------------------------------------------------------
    // NEW: добавляем схлопывание внутренних пробелов и трим краёв
    //      чтобы "beatles  " → "beatles",  "   " → "" , "ac   dc" → "ac dc"
    private fun normalizeQuery(raw: String): String =
        raw.trimForEmoji().replace(Regex("\\s+"), " ")

    // ⌛ триггеры поиска: дебаунс ввода + явный Done
    @OptIn(FlowPreview::class)
    private val debouncedQueries: Flow<String> =
        queryFlow
            .debounce(SEARCH_DEBOUNCE_DELAY)
            // NEW: нормализуем НА ВХОДЕ (см. onQueryChanged), здесь это больше не нужно
            .filter { it.isNotBlank() }
            .distinctUntilChanged()
            .onEach {
                removedFromSearch.value = emptySet()
                lastSubmittedQuery.value = it
            }

    /// 🔀 итоговый поток запросов: дебаунс + ручные повторы (без distinct!)
    private val searchQueries: Flow<String> =
        merge(
            debouncedQueries,
            manualSearchRequests.onEach { q ->
                removedFromSearch.value = emptySet()
                lastSubmittedQuery.value = q
            }
        )

    // 📝 реально выполненный запрос
    private val executedQuery: StateFlow<String> =
        searchQueries.stateIn(viewModelScope, SharingStarted.Eagerly, "")

    // 🔎 ресурс поиска
    @OptIn(ExperimentalCoroutinesApi::class)
    private val searchResource: Flow<Resource<List<Track>>> =
        searchQueries
            .flatMapLatest { q ->
                audioInteraction.searchTracks(q)
                    .onStart { emit(Resource.Success(emptyList())) } // ⏳ старт "реального" поиска
                    .catch { e -> emit(Resource.Error(e.message ?: "Unknown error")) }
            }
            .shareIn(viewModelScope, SharingStarted.Eagerly, replay = 1)

    // ⏳ индикатор загрузки: включаем на новый запрос, выключаем на первом реальном ответе
    private val loadingFlow: Flow<Boolean> =
        merge(
            searchQueries.map { true },
            searchResource.drop(1).map { false } // пропускаем onStart
        )

    // 👁️‍🗨️ вычисляем всё разом
    private data class Inputs(
        val query: String,
        val isFocused: Boolean,
        val history: List<Track>,
        val resource: Resource<List<Track>>,
        val isLoading: Boolean,
        val executedQuery: String,
        val removedIds: Set<Int>,
        val listTextColor: Int?,     // ⬅️ НОВОЕ
        val listArrowColor: Int?,     // ⬅️ НОВОЕ
        val listItemBackgroundColor: Int?
    )

    private data class P1(
        val query: String,
        val isFocused: Boolean,
        val history: List<Track>,
        val resource: Resource<List<Track>>,
        val isLoading: Boolean
    )

        private val inputs: Flow<Inputs> =
        combine(
            queryFlow,
            focusFlow,
            historyFlow,
            searchResource.onStart { emit(Resource.Success(emptyList())) },
            loadingFlow.onStart { emit(false) }
        ) { query, isFocused, history, resource, isLoading ->
            P1(query, isFocused, history, resource, isLoading)
        }
            // добавили executedQuery
            .combine(executedQuery) { p, execQ ->
                p to execQ
            }
            // добавили removedFromSearch → собрали Acc с пустыми цветами
            .combine(removedFromSearch) { (p, execQ), removedIds ->
                Acc(
                    p = p,
                    execQ = execQ,
                    removedIds = removedIds,
                    listTextColor = null,
                    listArrowColor = null
                )
            }
            // прокинули listTextColor
            .combine(listTextColorFlow) { acc, txtColor ->
                acc.copy(listTextColor = txtColor)
            }
            // прокинули listArrowColor
            .combine(listArrowColorFlow) { acc, arrowColor ->
                acc.copy(listArrowColor = arrowColor)
            }
            // И ТОЛЬКО ЗДЕСЬ собираем Inputs, уже имея всё + фон
            .combine(listItemBackgroundColorFlow) { acc, bgColor ->
                val p = acc.p
                Inputs(
                    query = p.query,
                    isFocused = p.isFocused,
                    history = p.history,
                    resource = p.resource,
                    isLoading = p.isLoading,
                    executedQuery = acc.execQ,
                    removedIds = acc.removedIds,                 // ← Set<Int> как надо
                    listTextColor = acc.listTextColor,
                    listArrowColor = acc.listArrowColor,
                    listItemBackgroundColor = bgColor            // ← фон (Int? из VM)
                )
            }

    // 🎛️ финальный UI-стейт
    val uiState: StateFlow<SearchUiState> =
        inputs
            .map { inp ->
                val queryBlank = inp.query.isBlank() // NEW: inp.query уже нормализован (единый источник истины)

                val showHistory = inp.isFocused && queryBlank && inp.history.isNotEmpty()
                // 🧠 queryBlank — пустой ввод?
                // 🗂️ showHistory — показывать историю только когда есть фокус, ввода нет и история не пуста

                // 1) 📦 Собираем «сырую» выдачу и базовую ошибку
                val (tracksRaw0, error0) = when (inp.resource) {
                    is Resource.Success -> {
                        val list = inp.resource.data.orEmpty()
                        // ❗ «Ничего не найдено» показываем ТОЛЬКО для реально выполненного запроса

                        // OLD: сравнивали сырую строку и выполненную → могли не совпасть из-за пробелов
                        // NEW: обе уже нормализованы → сравнение корректно

                        if (
                            inp.query.isNotEmpty() &&
                            inp.query == inp.executedQuery &&
                            !inp.isLoading &&
                            list.isEmpty()
                        ) {
                            emptyList<Track>() to ErrorState.ERROR // 🫙
                        } else {
                            list to ErrorState.NONE // ✅ есть данные или поиск ещё идёт
                        }
                    }
                    is Resource.Error -> emptyList<Track>() to ErrorState.FAILURE // ⚠️ сеть/сервер
                }

                // 2) 🧼 Мягкое удаление — прячем локально исключённые треки (без запроса к бэку)
                val filtered0 = tracksRaw0.filter { it.trackId !in inp.removedIds }

                // 3) 🚫 Жёсткая засечка: при пустом вводе — всегда пустой список и без ошибок
                val (tracksRaw, error) = if (queryBlank) {
                    emptyList<Track>() to ErrorState.NONE // 🔕 ни лоадера, ни ошибок, ни хвостов
                } else {
                    filtered0 to error0
                }

                // 4) 🎯 Что реально показываем пользователю
                val displayed = when {
                    showHistory -> inp.history                        // 🗂️ история
                    !queryBlank && inp.query != inp.executedQuery -> emptyList() // ⏳ печатает (debounce ещё не сработал)
                    else -> tracksRaw                                  // 🔍 свежая выдача поиска
                }

                // 5) 🧊 При пустом запросе не крутим лоадер
                val isLoadingSafe = if (queryBlank) false else inp.isLoading

                // 6) 🧱 Финальный UI-стейт (адаптер рисует displayedTracks)
                SearchUiState(
                    query = inp.query,
                    isInputFocused = inp.isFocused,
                    isClearIconVisible = inp.query.isNotEmpty(), // ❎ крестик только при непустом вводе
                    isLoading = isLoadingSafe,                   // ⏳ лоадер не мигает на пустом запросе
                    error = error,                               // 🚦 NONE / ERROR / FAILURE
                    searchTracks = tracksRaw,                    // 📄 сырая (отфильтрованная) выдача
                    historyTracks = inp.history,                 // 🗂️ история
                    showHistory = !isLoadingSafe && showHistory, // 👁️ история не перекрывается лоадером
                    displayedTracks = displayed,                  // 🖼️ именно это отображаем в списке
<<<<<<< Updated upstream
=======
                    // ⬇️ НОВОЕ
                    listTextColor = inp.listTextColor,
                    listArrowColor = inp.listArrowColor,
                    listItemBackgroundColor = inp.listItemBackgroundColor
>>>>>>> Stashed changes
                )
            }
            .stateIn(viewModelScope, SharingStarted.Eagerly, SearchUiState())


    // ─── public API ───

    // 2) При вводе — сразу нормализуем
    // -----------------------------------------------------------------------------

    fun onQueryChanged(query: String) {
        queryFlow.value = normalizeQuery(query)
    }

    fun setInputFocused(focused: Boolean) {
        focusFlow.value = focused
    }

    // 🔘 «Готово / Повторить»

    // -----------------------------------------------------------------------------

    fun onSearchActionDone() {
        val qUi = normalizeQuery(queryFlow.value)
        val q = if (qUi.isNotEmpty()) qUi else lastSubmittedQuery.value
        if (q.isNotEmpty()) manualSearchRequests.tryEmit(q)
    }

    fun clearSearchInput() {
        // только сброс — всё пересчитается в combine
        queryFlow.value = ""
    }

    fun onTrackClicked(track: Track) {
        viewModelScope.launch {
            searchHistoryInteraction.addTrackToHistory(track)
            _openTrack.tryEmit(track) // 🔔 событие навигации
        }
    }

    fun removeTrack(track: Track) {
        val state = uiState.value
        if (state.showHistory) {
            // 🗂️ удаляем из истории репозитория
            viewModelScope.launch {
                val updated = state.historyTracks.filter { it.trackId != track.trackId }
                searchHistoryInteraction.saveHistory(updated)
            }
        } else {
            // 🔎 мягко скрываем элемент из текущей выдачи
            removedFromSearch.value = removedFromSearch.value + track.trackId
        }
    }

    fun clearHistory() {
        viewModelScope.launch { searchHistoryInteraction.clearHistory() }
    }

    fun getTrackHistoryList(): List<Track> = uiState.value.historyTracks

    // сеттеры
    fun setListTextColor(color: Int?) {
        listTextColorFlow.value = color
    }
    // сеттеры
    fun setListArrowColor(color: Int?) {
        listArrowColorFlow.value = color
    }
    // сеттеры
    fun setListItemBackgroundColor(color: Int?) {
        listItemBackgroundColorFlow.value = color
    }

    // удобный общий сброс
    fun resetListColorsToDefault() {
        listItemBackgroundColorFlow.value = null
        listTextColorFlow.value = null
        listArrowColorFlow.value = null
    }
}
