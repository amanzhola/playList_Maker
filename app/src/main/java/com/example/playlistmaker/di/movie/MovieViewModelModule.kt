package com.example.playlistmaker.di.movie

import com.example.playlistmaker.presentation.movieCast.MoviesCastViewModel
import com.example.playlistmaker.presentation.movieDetails.AboutViewModel
import com.example.playlistmaker.presentation.movieDetails.MovieDetailsViewModel
import com.example.playlistmaker.presentation.movieDetails.PosterViewModel
import com.example.playlistmaker.presentation.movieNames.NamesViewModel
import com.example.playlistmaker.presentation.movieViewModels.MoviesViewModel
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
    viewModel {(movieId: String) ->
        AboutViewModel(movieId, get())
    }

    // add for Poster Fragments -> Poster
    viewModel {(posterUrl: String) ->
        PosterViewModel(posterUrl)
    }

    // add for Cast -> MoviesCastActivity
    viewModel { (movieId: String) ->
        MoviesCastViewModel(movieId, get())
    }

    // add for Persons
    viewModel {
        NamesViewModel(androidContext(), get())
    }
}
