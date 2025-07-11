package com.example.playlistmaker.di.movie

import com.example.playlistmaker.domain.api.movie.MoviesInteraction
import com.example.playlistmaker.domain.impl.movie.MoviesInteractionImpl
import com.example.playlistmaker.domain.impl.moviesDetails.MoviesInteractorImpl
import com.example.playlistmaker.domain.repository.movieDetails.MoviesInteractor
import org.koin.dsl.module

val movieInteractionModule = module { // 🎥 💃 🎬 // 🎥  from 🏠 🔍 🛠️ 🎧 ☁️

    // MoviesInteraction (MoviesViewModel)
    single<MoviesInteraction> { MoviesInteractionImpl(get()) }

    single<MoviesInteractor> {
        MoviesInteractorImpl(get()) // добавление для Постер Fragments
    }
}
