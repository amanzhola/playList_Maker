package com.example.playlistmaker.presentation.movieDetails

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.playlistmaker.domain.api.moviesDetials.PosterMovieRepository
import com.example.playlistmaker.domain.models.movieDetails.MovieDetails
import com.example.playlistmaker.domain.util.ResourceMovieDetials
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MovieDetailsViewModel(
    private val repository: PosterMovieRepository
) : ViewModel() {

    private val _movieDetails = MutableLiveData<ResourceMovieDetials<MovieDetails>>()
    val movieDetails: LiveData<ResourceMovieDetials<MovieDetails>> = _movieDetails

    fun loadMovieDetails(movieId: String) {
        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) {
                repository.getMovieDetails(movieId)
            }
            _movieDetails.value = result
        }
    }
}
