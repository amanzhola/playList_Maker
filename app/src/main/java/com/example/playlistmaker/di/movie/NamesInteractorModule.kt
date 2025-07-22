package com.example.playlistmaker.di.movie

import com.example.playlistmaker.domain.api.moviePersons.NamesInteractor
import com.example.playlistmaker.domain.impl.moviePersons.NamesInteractorImpl
import org.koin.dsl.module

val namesInteractorModule = module {// 🎥 💃 🎬

    // Интерактор для работы с актёрами / кастом
    single<NamesInteractor> {
        NamesInteractorImpl(get())
    }
}