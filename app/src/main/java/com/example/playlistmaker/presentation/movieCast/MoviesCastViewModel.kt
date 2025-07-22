package com.example.playlistmaker.presentation.movieCast

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.playlistmaker.domain.models.movieCast.MovieCast
import com.example.playlistmaker.domain.repository.movieDetails.MoviesInteractor
import com.example.playlistmaker.ui.movieCast.MoviesCastRVItem

// В конструктор пробросили необходимые для запроса параметры
class MoviesCastViewModel(
    private val movieId: String,
    private val moviesInteractor: MoviesInteractor,
) : ViewModel() {

    // Стандартная обвязка для определения State
    // и наблюдения за ним в UI-слое
    private val stateLiveData = MutableLiveData<MoviesCastState>()
    fun observeState(): LiveData<MoviesCastState> = stateLiveData

//    init {
//        // При старте экрана покажем ProgressBar
//        stateLiveData.postValue(MoviesCastState.Loading)
//
//        // Выполняем сетевой запрос
//        moviesInteractor.getMovieCast(movieId, object : MoviesInteractor.MovieCastConsumer {
//
//            // Обрабатываем результат этого запроса
//            override fun consume(movieCast: MovieCast?, errorMessage: String?) {
//                if (movieCast != null) {
//                    stateLiveData.postValue(MoviesCastState.Content(movieCast))
//                } else {
//                    stateLiveData.postValue(MoviesCastState.Error(errorMessage ?: "Unknown error"))
//                }
//            }
//
//        })
//    }

    init {
        stateLiveData.postValue(MoviesCastState.Loading)

        moviesInteractor.getMovieCast(movieId, object : MoviesInteractor.MovieCastConsumer {

            override fun consume(movieCast: MovieCast?, errorMessage: String?) {
                if (movieCast != null) {
                    // добавляем конвертацию в UiState
                    stateLiveData.postValue(castToUiStateContent(movieCast))
                } else {
                    stateLiveData.postValue(MoviesCastState.Error(errorMessage ?: "Unknown error"))
                }
            }

        })
    }

    private fun castToUiStateContent(cast: MovieCast): MoviesCastState {
        // Строим список элементов RecyclerView
        val items = buildList<MoviesCastRVItem> {
            // Если есть хотя бы один режиссёр, добавим заголовок
            if (cast.directors.isNotEmpty()) {
                this += MoviesCastRVItem.HeaderItem("Directors")
                this += cast.directors.map { MoviesCastRVItem.PersonItem(it) }
            }

            // Если есть хотя бы один сценарист, добавим заголовок
            if (cast.writers.isNotEmpty()) {
                this += MoviesCastRVItem.HeaderItem("Writers")
                this += cast.writers.map { MoviesCastRVItem.PersonItem(it) }
            }

            // Если есть хотя бы один актёр, добавим заголовок
            if (cast.actors.isNotEmpty()) {
                this += MoviesCastRVItem.HeaderItem("Actors")
                this += cast.actors.map { MoviesCastRVItem.PersonItem(it) }
            }

            // Если есть хотя бы один дополнительный участник, добавим заголовок
            if (cast.others.isNotEmpty()) {
                this += MoviesCastRVItem.HeaderItem("Others")
                this += cast.others.map { MoviesCastRVItem.PersonItem(it) }
            }
        }


        return MoviesCastState.Content(
            fullTitle = cast.fullTitle,
            items = items
        )
    }
}