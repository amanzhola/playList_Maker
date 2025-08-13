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

    // 🧩 ввод и фокус
    private val queryFlow = MutableStateFlow("")
    private val focusFlow = MutableStateFlow(false)

    // 🙋 ручной "поиск по Done" (если понадобится)
    private val manualSearchRequests = MutableSharedFlow<String>(extraBufferCapacity = 1)

    // ⛔ локально скрытые треки из текущей выдачи
    private val removedFromSearch = MutableStateFlow<Set<Int>>(emptySet())

    // 🕘 История: сначала снимок, затем “живая” подписка
    private val historyFlow: Flow<List<Track>> =
        searchHistoryInteraction
            .observeHistory()
            .onStart { emit(searchHistoryInteraction.getHistory()) }

    // 🔤 без поломки emoji — режем только обычные пробелы/переносы
    private fun String.trimForEmoji(): String = trim(' ', '\t', '\n', '\r')

    // ⌛ триггеры поиска: дебаунс ввода + явный Done
    @OptIn(FlowPreview::class)
    private val searchQueries: Flow<String> =
        merge(
            queryFlow
                .debounce(SEARCH_DEBOUNCE_DELAY)
                .map { it.trimForEmoji() }
                .filter { it.isNotBlank() }
                .onEach { removedFromSearch.value = emptySet() },
            manualSearchRequests.onEach { removedFromSearch.value = emptySet() }
        ).distinctUntilChanged()

    // 📝 реально выполненный запрос (после дебаунса)
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
        val removedIds: Set<Int>
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
            .combine(executedQuery) { p, execQ ->
                p to execQ
            }
            .combine(removedFromSearch) { (p, execQ), removedIds ->
                Inputs(
                    query = p.query,
                    isFocused = p.isFocused,
                    history = p.history,
                    resource = p.resource,
                    isLoading = p.isLoading,
                    executedQuery = execQ,
                    removedIds = removedIds
                )
            }

    // 🎛️ финальный UI-стейт
    val uiState: StateFlow<SearchUiState> =
        inputs
            .map { inp ->
                val showHistory = inp.isFocused && inp.query.isEmpty() && inp.history.isNotEmpty()

                // получаем сырую выдачу
                val (tracksRaw, error) = when (inp.resource) {
                    is Resource.Success -> {
                        val list = inp.resource.data.orEmpty()
                        // «ничего не найдено» показываем ТОЛЬКО когда запрос реально выполнен
                        // (query == executedQuery), загрузка завершилась и итог пуст
                        if (
                            inp.query.isNotEmpty() &&
                            inp.query == inp.executedQuery &&
                            !inp.isLoading &&
                            list.isEmpty()
                        ) {
                            emptyList<Track>() to ErrorState.ERROR
                        } else {
                            list to ErrorState.NONE
                        }
                    }
                    is Resource.Error -> emptyList<Track>() to ErrorState.FAILURE
                }

                // локальная фильтрация (мягкое удаление)
                val filtered = tracksRaw.filter { it.trackId !in inp.removedIds }

                // какие треки реально показываем:
                // 1) если история активна — история
                // 2) если ввод идёт или debounce ещё не кончился (query != executedQuery) — пусто
                // 3) иначе — актуальная отфильтрованная выдача
                val displayed = when {
                    showHistory -> inp.history
                    inp.query.isNotEmpty() && inp.query != inp.executedQuery -> emptyList()
                    else -> filtered
                }

                SearchUiState(
                    query = inp.query,
                    isInputFocused = inp.isFocused,
                    isClearIconVisible = inp.query.isNotEmpty(),
                    isLoading = inp.isLoading,
                    error = error,
                    searchTracks = filtered,         // можно оставить для совместимости
                    historyTracks = inp.history,
                    showHistory = !inp.isLoading && showHistory,
                    displayedTracks = displayed      // 👈 важно: именно это рисует адаптер
                )
            }
            .stateIn(viewModelScope, SharingStarted.Eagerly, SearchUiState())

    // ─── public API ───

    fun onQueryChanged(query: String) {
        queryFlow.value = query
    }

    fun setInputFocused(focused: Boolean) {
        focusFlow.value = focused
    }

    fun onSearchActionDone() {
        val q = queryFlow.value.trimForEmoji()
        if (q.isNotEmpty()) manualSearchRequests.tryEmit(q)
    }

    fun clearSearchInput() {
        // только сброс — всё пересчитается в combine
        queryFlow.value = ""
    }

    fun onTrackClicked(track: Track) {
        viewModelScope.launch { searchHistoryInteraction.addTrackToHistory(track) }
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
}
