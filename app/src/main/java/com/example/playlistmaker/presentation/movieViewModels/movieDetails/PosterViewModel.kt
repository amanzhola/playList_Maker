package com.example.playlistmaker.presentation.movieViewModels.movieDetails

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class PosterViewModel(private val posterUrl: String) : ViewModel() {

    private val urlLiveData = MutableLiveData(posterUrl)
    fun observeUrl(): LiveData<String> = urlLiveData
}