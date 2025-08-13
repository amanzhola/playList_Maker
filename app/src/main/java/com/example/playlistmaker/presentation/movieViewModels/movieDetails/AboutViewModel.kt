package com.example.playlistmaker.presentation.movieViewModels.movieDetails

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.playlistmaker.domain.repository.movieDetails.MoviesInteractor
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AboutViewModel(
    private val movieId: String,
    private val moviesInteractor: MoviesInteractor,
) : ViewModel() {

    // 🔁 триггер загрузки (replay=1 не нужен — стартуем вручную в init-потоке ниже)
    private val refresh = MutableSharedFlow<Unit>(replay = 0)

    // 🖼️ единый UI-стейт
    @OptIn(ExperimentalCoroutinesApi::class)
    val state: StateFlow<AboutState> =
        refresh
            .onStart { emit(Unit) } // 🚀 первый запуск при подписке (без init {})
            .flatMapLatest {
                moviesInteractor.getMoviesDetails(movieId)
                    .map { pair ->
                        val movieDetails = pair.first
                        val errorMessage = pair.second
                        if (movieDetails != null) {
                            AboutState.Content(movieDetails)
                        } else {
                            AboutState.Error(errorMessage ?: "Unknown error")
                        }
                    }
                    .onStart { emit(AboutState.Loading) } // ⏳ показываем лоадер до ответа
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.Eagerly,
                initialValue = AboutState.Idle
            )

    // 🔄 ручной рефреш (если нужно дернуть повторно)
    fun refresh() {
        viewModelScope.launch { refresh.emit(Unit) }
    }
}
