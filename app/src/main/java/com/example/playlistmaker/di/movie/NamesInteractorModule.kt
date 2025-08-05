package com.example.playlistmaker.di.movie

import com.example.playlistmaker.domain.api.moviePersons.NamesInteractor
import com.example.playlistmaker.domain.api.movie_db.HistoryInteractor
import com.example.playlistmaker.domain.impl.moviePersons.NamesInteractorImpl
import com.example.playlistmaker.domain.impl.movie_db.HistoryInteractorImpl
import org.koin.dsl.module

val namesInteractorModule = module {// 🎥 💃 🎬

    // Интерактор для работы с актёрами / кастом
    single<NamesInteractor> {
        NamesInteractorImpl(get())
    }

    // Movies Data Base
    single<HistoryInteractor> {
        HistoryInteractorImpl(get())
    }
}