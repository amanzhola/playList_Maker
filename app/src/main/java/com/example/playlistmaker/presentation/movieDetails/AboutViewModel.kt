package com.example.playlistmaker.presentation.movieDetails

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.playlistmaker.domain.models.movieDetails.MovieDetails
import com.example.playlistmaker.domain.repository.base.movieDetails.MoviesInteractor

class AboutViewModel(private val movieId: String,
                     private val moviesInteractor: MoviesInteractor, ) : ViewModel() {

    private val stateLiveData = MutableLiveData<AboutState>()
    fun observeState(): LiveData<AboutState> = stateLiveData

    init {

//        Log.d("AboutViewModel", "Created with movieId=$movieId")
        moviesInteractor.getMoviesDetails(movieId, object : MoviesInteractor.MovieDetailsConsumer {

            override fun consume(movieDetails: MovieDetails?, errorMessage: String?) {
                if (movieDetails != null) {
                    stateLiveData.postValue(AboutState.Content(movieDetails))
                } else {
                    stateLiveData.postValue(AboutState.Error(errorMessage ?: "Unknown error"))
                }
            }
        })
    }
}