package com.example.playlistmaker.presentation.movieViewModels.movieDetails

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.playlistmaker.domain.api.moviesDetails.PosterMovieRepository
import com.example.playlistmaker.domain.util.ResourceMovieDetials
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch


class MovieDetailsViewModel(
    private val repository: PosterMovieRepository
) : ViewModel() {

    private val _movieDetails = MutableLiveData<AboutState>()
    val movieDetails: LiveData<AboutState> = _movieDetails

    fun loadMovieDetails(movieId: String) {
        viewModelScope.launch {
            repository.getMovieDetails(movieId)
                .map { result ->
                    when (result) {
                        is ResourceMovieDetials.Success -> Pair(result.data, null)
                        is ResourceMovieDetials.Error -> Pair(null, result.message)
                    }
                }
                .collect { pair ->
                    val movieDetails = pair.first
                    val errorMessage = pair.second

                    if (movieDetails != null) {
                        _movieDetails.value = AboutState.Content(movieDetails)
                    } else {
                        _movieDetails.value = AboutState.Error(errorMessage ?: "Unknown error")
                    }
                }
        }
    }
}
