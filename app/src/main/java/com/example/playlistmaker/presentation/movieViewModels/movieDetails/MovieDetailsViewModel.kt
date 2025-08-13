package com.example.playlistmaker.presentation.movieViewModels.movieDetails

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.playlistmaker.domain.api.moviesDetails.PosterMovieRepository
import com.example.playlistmaker.domain.models.movieDetails.MovieDetails
import com.example.playlistmaker.domain.util.ResourceMovieDetials
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MovieDetailsViewModel(
    private val repository: PosterMovieRepository
) : ViewModel() {

    // 🆔 текущее id фильма
    private val movieId = MutableStateFlow<String?>(null)

    // 🔁 триггер перезагрузки (в том числе первой)
    private val refresh = MutableSharedFlow<Unit>(replay = 0)

    // 🖼️ единый UI-стейт
    @OptIn(ExperimentalCoroutinesApi::class)
    val state: StateFlow<AboutState> =
        refresh
            .onStart { emit(Unit) }                      // 🚀 автозапуск при первой подписке
            .combine(movieId) { _, id -> id }           // берём текущее id при каждом refresh
            .flatMapLatest { id ->
                if (id.isNullOrBlank()) {
                    flowOf<AboutState>(AboutState.Idle)  // до запроса — тишина
                } else {
                    repository.getMovieDetails(id)
                        .map< ResourceMovieDetials<MovieDetails>, AboutState> { result ->
                            when (result) {
                                is ResourceMovieDetials.Success ->
                                    AboutState.Content(result.data)
                                is ResourceMovieDetials.Error ->
                                    AboutState.Error(result.message ?: "Unknown error")
                            }
                        }
                        .onStart { emit(AboutState.Loading) } // ⏳ показываем лоадер до ответа
                }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.Eagerly,
                initialValue = AboutState.Idle
            )

    // 📥 загрузка деталей по id
    fun loadMovieDetails(id: String) {
        movieId.value = id
        viewModelScope.launch { refresh.emit(Unit) } // «пинаем» загрузку для нового id
    }

    // 🔄 ручной рефреш (повторить запрос для того же id)
    fun refresh() {
        viewModelScope.launch { refresh.emit(Unit) }
    }
}
