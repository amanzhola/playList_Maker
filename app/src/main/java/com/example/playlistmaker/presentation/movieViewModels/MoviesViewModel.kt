package com.example.playlistmaker.presentation.movieViewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.playlistmaker.domain.api.movie.MoviesInteraction
import com.example.playlistmaker.domain.models.movie.Movie
import com.example.playlistmaker.domain.usecases.movie.ToggleFavoriteUseCase
import com.example.playlistmaker.domain.util.Resource
import com.example.playlistmaker.utils.SEARCH_DEBOUNCE_DELAY
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn

class MoviesViewModel(
    private val moviesInteraction: MoviesInteraction,     // 🌐🔎 доменный поиск фильмов
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase // 🧠❤️ юзкейс переключения избранного
) : ViewModel() {

    // 🎛️ UI стейты — единый источник правды для экрана
    sealed class UiState {
        data object Default : UiState()                         // 💤 пустой ввод / стартовый экран
        data object Loading : UiState()                         // ⏳ загрузка
        data class Success(val movies: List<Movie>) : UiState() // ✅ данные получены
        data class Error(val message: String) : UiState()       // ❌ ошибка запроса/сети
        data object Empty : UiState()                           // 🫙 ничего не найдено
    }

    // ── Query ───────────────────────────────────────────────────────────────────
    // 🧩 ввод из UI (горячий поток, всегда хранит последнее значение)
    private val queryFlow = MutableStateFlow("")               // 🔥 StateFlow внутри
    val query: StateFlow<String> = queryFlow.asStateFlow()     // 🧊 наружу только чтение (инкапсуляция)

    // 🗂️ текущий отображаемый список; нужен для мгновенных локальных обновлений
    // ✋ локальная подмена после клика по ❤️; при новом поиске очищается
    private val _movies = MutableStateFlow<List<Movie>>(emptyList())
    val movies: StateFlow<List<Movie>> = _movies.asStateFlow()

    // UiState строится без init: вся логика дебаунса/поиска — в декларативном пайплайне
    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<UiState> =
        queryFlow
            .map { it.trim() }                                  // ✂️ убираем лишние пробелы
            .debounce(SEARCH_DEBOUNCE_DELAY)                    // ⏱️ ждём, пока пользователь «допечатает»
            .distinctUntilChanged()                             // 🔁🚫 не дёргаем поиск на одинаковый ввод
            .flatMapLatest { q ->                               // 🏎️💨 отменяем старый поиск при новом вводе
                if (q.isEmpty()) {
                    _movies.value = emptyList()                 // 🧹 чистим локальный список
                    flowOf(UiState.Default)                     // 📭 показываем дефолтный экран
                } else {
                    moviesInteraction.searchMovies(q)           // 🌐🔎 доменный Flow<Resource<List<Movie>>>
                        .map { result ->                        // 🧰 трансформируем ресурс в UiState
                            when (result) {
                                is Resource.Success -> {        // 📬 успех
                                    val list = result.data.orEmpty()
                                    if (list.isEmpty()) {
                                        _movies.value = emptyList()
                                        UiState.Empty           // 🫙 пустая выдача
                                    } else {
                                        val favoriteIds = toggleFavoriteUseCase.getFavorites() // 📥❤️ получаем избранные id
                                        val updated = list
                                            .map { m -> m.copy(inFavorite = favoriteIds.contains(m.id)) } // 🖍️ помечаем ❤️
                                            .sortedByDescending { it.inFavorite }                         // ❤️🔝 избранные вверх
                                        _movies.value = updated
                                        UiState.Success(updated) // ✅ отдаём на экран
                                    }
                                }
                                is Resource.Error -> {           // 🆘 ошибка доменного слоя/сети
                                    _movies.value = emptyList()
                                    UiState.Error(result.message ?: "Неизвестная ошибка") // 🛟 сообщение о проблеме
                                }
                            }
                        }
                        .onStart { emit(UiState.Loading) }       // 🎬⏳ перед реальным ответом покажем загрузку
                }
            }
            .stateIn(
                scope = viewModelScope,                          // 🧵 жизненный цикл корутин = ViewModel
                started = SharingStarted.WhileSubscribed(5_000), // 👂 активен, пока есть подписчики (ещё +5с)
                initialValue = UiState.Default                   // 🍼 начальное состояние
            )

    // ── 📣 Public API ───────────────────────────────────────────────────────────

    fun onSearchQueryEntered(rawQuery: String) {  // ⌨️📨 ввод из UI
        queryFlow.value = rawQuery                // 🧲 триггерим пайплайн сверху
    }

    fun toggleFavorite(movieId: String) {         // ❤️ клик по избранному
        val current = _movies.value
        if (current.isEmpty()) return             // 🚪 нечего обновлять

        val updated = current.map { movie ->
            if (movie.id == movieId) {
                toggleFavoriteUseCase(movie.id)   // 💾 side-effect: сохранить новое состояние
                movie.copy(inFavorite = !movie.inFavorite) // 🔁 flip ❤️
            } else movie
        }.sortedByDescending { it.inFavorite }    // ❤️🔝 держим избранные сверху

        _movies.value = updated                   // 🚀 моментально обновляем список на экране
        // 📝 UiState остаётся Success — переэмичивать не нужно
    }

    fun refreshFavorites() {                      // 🔄❤️ синхронизировать с хранилищем (после деталей и т.п.)
        val updated = _movies.value
            .map { it.copy(inFavorite = toggleFavoriteUseCase.isFavorite(it.id)) } // 🧾 сверка
            .sortedByDescending { it.inFavorite }                                   // ❤️🔝
        _movies.value = updated
    }

    fun setDefaultState() {                       // 🧼 полный сброс (крестик/уход со страницы)
        queryFlow.value = ""                      // 🧼❎ очистить строку поиска
        _movies.value = emptyList()               // 🧼🗂️ очистить список
    }
}
/*
легенда (на всякий):
⏱️ debounce • 🏎️💨 flatMapLatest • 🔁🚫 distinctUntilChanged
🔥/🧊 StateFlow внутрь/наружу • 🧹 сброс • 📬 успех • 🆘/🛟 ошибка
❤️ избранное • 💾 запись/side-effect • 🔝 сортировка вверх
*/