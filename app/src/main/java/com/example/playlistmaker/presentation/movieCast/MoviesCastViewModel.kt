package com.example.playlistmaker.presentation.movieCast

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.playlistmaker.domain.models.movieCast.MovieCast
import com.example.playlistmaker.domain.repository.movieDetails.MoviesInteractor
import com.example.playlistmaker.ui.movieCast.MoviesCastRVItem
import kotlinx.coroutines.launch

class MoviesCastViewModel(
    private val movieId: String,
    private val moviesInteractor: MoviesInteractor,
) : ViewModel() {

    private val stateLiveData = MutableLiveData<MoviesCastState>()
    fun observeState(): LiveData<MoviesCastState> = stateLiveData

    init {
        stateLiveData.postValue(MoviesCastState.Loading)

        viewModelScope.launch {
            moviesInteractor.getMovieCast(movieId).collect { pair ->
                val movieCast = pair.first
                val errorMessage = pair.second

                if (movieCast != null) {
                    stateLiveData.postValue(castToUiStateContent(movieCast))
                } else {
                    stateLiveData.postValue(MoviesCastState.Error(errorMessage ?: "Unknown error"))
                }
            }
        }
    }

    private fun castToUiStateContent(cast: MovieCast): MoviesCastState {
        val items = buildList<MoviesCastRVItem> {
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

        return MoviesCastState.Content(
            fullTitle = cast.fullTitle,
            items = items
        )
    }
}
