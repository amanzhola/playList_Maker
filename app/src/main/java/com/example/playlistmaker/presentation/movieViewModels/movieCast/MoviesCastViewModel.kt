package com.example.playlistmaker.presentation.movieViewModels.movieCast

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.playlistmaker.domain.models.movieCast.MovieCast
import com.example.playlistmaker.domain.repository.movieDetails.MoviesInteractor
import com.example.playlistmaker.ui.core.ui.RVItem
import com.example.playlistmaker.ui.movie.movieCast.MoviesCastRVItem
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MoviesCastViewModel(
    private val movieId: String,
    private val moviesInteractor: MoviesInteractor,
) : ViewModel() {

    // 🔁 триггеры загрузки (и для первого старта, и для ручного refresh)
    private val refresh = MutableSharedFlow<Unit>(replay = 0)

    // 🖼️ Единый UI-стейт
    @OptIn(ExperimentalCoroutinesApi::class)
    val state: StateFlow<MoviesCastState> =
        refresh
            .onStart { emit(Unit) } // 🚀 автозапуск без init{}
            .flatMapLatest {
                moviesInteractor.getMovieCast(movieId)
                    .map { pair ->
                        val movieCast = pair.first
                        val errorMessage = pair.second
                        if (movieCast != null) {
                            castToUiStateContent(movieCast)
                        } else {
                            MoviesCastState.Error(errorMessage ?: "Unknown error")
                        }
                    }
                    .onStart { emit(MoviesCastState.Loading) } // ⏳ пока ждём данные
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.Eagerly,
                initialValue = MoviesCastState.Loading
            )

    // 🔄 публичный рефреш (по свайпу/кнопке — если нужно)
    fun refresh() {
        viewModelScope.launch { refresh.emit(Unit) }
    }

    // 🧩 маппинг доменной модели в список ячеек для адаптера
    private fun castToUiStateContent(cast: MovieCast): MoviesCastState {
        val items: List<RVItem> = buildList {
            if (cast.directors.isNotEmpty()) {
                add(MoviesCastRVItem.HeaderItem("Directors"))
                addAll(cast.directors.map { MoviesCastRVItem.PersonItem(it) })
            }
            if (cast.writers.isNotEmpty()) {
                add(MoviesCastRVItem.HeaderItem("Writers"))
                addAll(cast.writers.map { MoviesCastRVItem.PersonItem(it) })
            }
            if (cast.actors.isNotEmpty()) {
                add(MoviesCastRVItem.HeaderItem("Actors"))
                addAll(cast.actors.map { MoviesCastRVItem.PersonItem(it) })
            }
            if (cast.others.isNotEmpty()) {
                add(MoviesCastRVItem.HeaderItem("Others"))
                addAll(cast.others.map { MoviesCastRVItem.PersonItem(it) })
            }
        }
        // ⚠️ предполагается, что MoviesCastRVItem.* реализуют RVItem.
        return MoviesCastState.Content(
            fullTitle = cast.fullTitle,
            items = items
        )
    }
}
