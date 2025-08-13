package com.example.playlistmaker.di.movie

import com.example.playlistmaker.presentation.movieViewModels.MoviesViewModel
import com.example.playlistmaker.presentation.movieViewModels.movieCast.MoviesCastViewModel
import com.example.playlistmaker.presentation.movieViewModels.movieDetails.AboutViewModel
import com.example.playlistmaker.presentation.movieViewModels.movieDetails.MovieDetailsViewModel
import com.example.playlistmaker.presentation.movieViewModels.movieDetails.PosterViewModel
import com.example.playlistmaker.presentation.movieViewModels.movieHistory.HistoryViewModel
import com.example.playlistmaker.presentation.movieViewModels.movieNames.NamesViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val movieViewModelModule = module { // 🎥 💃 🎬 // 🎥  from 🏠 🔍 🛠️ 🎧 ☁️

    // MoviesViewModel (MoviesViewModel)
    viewModel { MoviesViewModel(get(), get()) }

    // add for Poster Fragments -> MovieDetails
    viewModel {
        MovieDetailsViewModel(get()) // PosterMovieRepository
    }

    // add for Poster Fragments -> About
    // AboutViewModel c параметром movieId
    viewModel {(movieId: String) ->
        AboutViewModel(movieId, get())
    }

    // add for Poster Fragments -> Poster
    // Вариант 2 (с SavedStateHandle)
    viewModel { (posterUrl: String) ->
        PosterViewModel(posterUrl, get()) // get<SavedStateHandle>()
    }

    // add for Cast -> MoviesCastActivity
    viewModel { (movieId: String) ->
        MoviesCastViewModel(movieId, get())
    }

    // add for Persons
    viewModel {
        NamesViewModel(androidContext(), get())
    }

    // Movies Data Base
    viewModel {
        HistoryViewModel(androidContext(), get())
    }
}
