package com.example.playlistmaker.presentation.movieViewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.playlistmaker.domain.api.movie.MoviesInteraction
import com.example.playlistmaker.domain.models.movie.Movie
import com.example.playlistmaker.domain.usecases.movie.ToggleFavoriteUseCase
import com.example.playlistmaker.domain.util.Resource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
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

private const val TAG_HISTORY_VM = "HistoryVM"

class MoviesViewModel(
    private val moviesInteraction: MoviesInteraction,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase
) : ViewModel() {

    // 🎛️ UI стейты
    sealed class UiState {
        data object Default : UiState()          // 💤 пустой ввод / стартовый экран
        data object Loading : UiState()          // ⏳ загрузка
        data class Success(val movies: List<Movie>) : UiState() // ✅ данные
        data class Error(val message: String) : UiState()       // ❌ ошибка
        data object Empty : UiState()            // 🫙 ничего не найдено
    }

    // 🧩 ввод из UI
    private val queryFlow = MutableStateFlow("")

    // ✋ локальная «ручная» подмена списка (после клика по ❤️), очищается при новом запросе
    private val manualMovies = MutableStateFlow<List<Movie>?>(null)

    // 🔤 безопасный trim для эмодзи: убираем только обычные пробелы/переводы строк
    private fun String.trimForEmoji(): String = trim(' ', '\t', '\n', '\r')

    // ⌛ дебаунс + фильтр пустых, без поломки emoji
    @OptIn(FlowPreview::class)
    private val searchQueries: Flow<String> =
        queryFlow
//            .debounce(SEARCH_DEBOUNCE_DELAY)
            .map { it.trimForEmoji() }                 // 👈 эмодзи остаются целыми
            .filter { it.isNotEmpty() }
            .distinctUntilChanged()
            .onEach { manualMovies.value = null }      // ♻️ новый запрос → сбрасываем локальные правки

    // 📝 фактически ВЫПОЛНЕННЫЙ запрос (после дебаунса)
    private val executedQuery: StateFlow<String> =
        searchQueries.stateIn(viewModelScope, SharingStarted.Eagerly, "")

//    // 🔎 поиск фильмов (ресурс)
    @OptIn(ExperimentalCoroutinesApi::class)
    private val searchResource: Flow<Resource<List<Movie>>> =
        searchQueries
            .flatMapLatest { q ->
                moviesInteraction.searchMovies(q)
                    .onStart { emit(Resource.Success(emptyList())) } // ⏳ лоадер только после дебаунса
                    .catch { e -> emit(Resource.Error(e.message ?: "Unknown error")) }
            }
            .shareIn(viewModelScope, SharingStarted.Eagerly, replay = 1)


    // ⏳ лоадер: стартуем на новом запросе, гасим на ПЕРВОМ реальном ответе (после onStart)
    private val loadingFlow: Flow<Boolean> =
        merge(
            searchQueries.map { true },
            searchResource.drop(1).map { false } // 👈 пропускаем onStart
        )

    // ❤️ накатываем «избранное» на выдачу поиска
    private val moviesFromSearch: Flow<List<Movie>> =
        searchResource.map { res ->
            val base = when (res) {
                is Resource.Success -> res.data.orEmpty()
                is Resource.Error   -> emptyList()
            }
            val favoriteIds = toggleFavoriteUseCase.getFavorites()
            base.map { m -> m.copy(inFavorite = favoriteIds.contains(m.id)) }
                .sortedByDescending { it.inFavorite } // ❤️🔝 сначала избранные
        }

    // 🕐 «pending» — печатаем, но дебаунс ещё не отдал запрос (query != executedQuery)
    private val pendingSearch: Flow<Boolean> =
        combine(queryFlow, executedQuery) { q, execQ ->
            val t = q.trimForEmoji()
            t.isNotBlank() && t != execQ
        }.distinctUntilChanged()

    // 👀 «видимый» список для UI: если query пустой ИЛИ запрос ещё не стартовал → пусто
    private val visibleMovies: Flow<List<Movie>> =
        combine(queryFlow, executedQuery, manualMovies, moviesFromSearch) { q, execQ, manual, fromSearch ->
            val t = q.trimForEmoji()
            if (t.isBlank() || t != execQ) emptyList() else manual ?: fromSearch
        }

    // 🧮 сборка входов
    private data class Inputs(
        val query: String,
        val isLoading: Boolean,
        val isPending: Boolean,
        val resource: Resource<List<Movie>>,
        val visibleMovies: List<Movie>,
        val executedQuery: String
    )

    // небольшие «контейнеры», чтобы не городить Pair<Pair<...>>
    private data class QLP( // Query + Loading + Pending
        val query: String,
        val isLoading: Boolean,
        val isPending: Boolean
    )
    private data class QLPRes(
        val base: QLP,
        val resource: Resource<List<Movie>>
    )
    private data class QLPResMovies(
        val base: QLP,
        val resource: Resource<List<Movie>>,
        val movies: List<Movie>
    )

    // 1) комбинируем первые три
    private val qlp: Flow<QLP> = combine(
        queryFlow,
        loadingFlow.onStart { emit(false) },
        pendingSearch.onStart { emit(false) }
    ) { q, isLoading, isPending ->
        QLP(q, isLoading, isPending)
    }

    // 2) добавляем resource
    private val qlpRes: Flow<QLPRes> = qlp.combine(searchResource) { base, res ->
        QLPRes(base, res)
    }

    // 3) добавляем видимые фильмы
    private val qlpResMovies: Flow<QLPResMovies> = qlpRes.combine(visibleMovies) { br, movies ->
        QLPResMovies(br.base, br.resource, movies)
    }

    // 4) финально приклеиваем executedQuery и строим Inputs
    private val baseInputs: Flow<Inputs> = qlpResMovies.combine(executedQuery) { brm, execQ ->
        Inputs(
            query = brm.base.query,
            isLoading = brm.base.isLoading,
            isPending = brm.base.isPending,
            resource = brm.resource,
            visibleMovies = brm.movies,
            executedQuery = execQ
        )
    }

    // 🖼️ конечный UI-стейт
    val uiState: StateFlow<UiState> =
        baseInputs
            .map { inp ->
                val q = inp.query.trimForEmoji()
                when {
                    q.isBlank()       -> UiState.Default                      // 💤 пустой запрос
//                    inp.isPending     -> UiState.Loading                      // ⏳ печатаем, но поиск ещё не ушёл
                    // БЫЛО: inp.isPending -> UiState.Loading
                    // СТАЛО: пока печатает, ничего не показываем (держим Default)
                    inp.isPending     -> UiState.Default // ⏳ печатаем, но поиск ещё не ушёл
                    inp.isLoading     -> UiState.Loading                      // ⏳ ждём ответа
                    inp.resource is Resource.Error ->
                        UiState.Error(inp.resource.message ?: "Unknown error")// ❌ ошибка
                    // 🫙 «ничего не найдено» — только для актуального и завершённого запроса
                    inp.executedQuery.isNotEmpty() &&
                            q == inp.executedQuery &&
                            !inp.isLoading &&
                            inp.visibleMovies.isEmpty() -> UiState.Empty
                    else -> UiState.Success(inp.visibleMovies)                // ✅ данные
                }
            }
            .stateIn(viewModelScope, SharingStarted.Eagerly, UiState.Default)

    // ── 📣 public API ──

    fun onSearchQueryEntered(rawQuery: String) { // ⌨️ ввод из UI
        queryFlow.value = rawQuery
    }

    fun toggleFavorite(movieId: String) { // ❤️ клик по избранному
        toggleFavoriteUseCase(movieId)
        val current = (uiState.value as? UiState.Success)?.movies ?: return
        val updated = current.map { m ->
            if (m.id == movieId) m.copy(inFavorite = !m.inFavorite) else m
        }.sortedByDescending { it.inFavorite }
        manualMovies.value = updated
    }

    fun refreshFavorites() { // 🔁 актуализировать избранное (после детального экрана и т.п.)
        val current = (uiState.value as? UiState.Success)?.movies ?: return
        val updated = current.map { m ->
            m.copy(inFavorite = toggleFavoriteUseCase.isFavorite(m.id))
        }.sortedByDescending { it.inFavorite }
        manualMovies.value = updated
    }

    fun setDefaultState() { // 🧹 полный сброс (крестик/уход со страницы)
        queryFlow.value = ""
        manualMovies.value = null
    }
}
