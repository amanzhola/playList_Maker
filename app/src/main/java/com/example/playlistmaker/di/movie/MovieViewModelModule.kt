package com.example.playlistmaker.di.movie

import com.example.playlistmaker.presentation.movieViewModels.MoviesViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val movieViewModelModule = module { // 🎥 💃 🎬 // 🎥  from 🏠 🔍 🛠️ 🎧 ☁️

    // MoviesViewModel (MoviesViewModel)
    viewModel { MoviesViewModel(get(), get()) }
}
