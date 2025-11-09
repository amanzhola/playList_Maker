package com.example.playlistmaker.di.movie

import com.example.playlistmaker.data.network.movieDetails.NetworkClient
import com.example.playlistmaker.data.network.movieDetails.RetrofitNetworkClient
import com.example.playlistmaker.data.repository.moviePersons.NamesRepositoryImpl
import com.example.playlistmaker.domain.api.moviePersons.NamesRepository
import org.koin.dsl.module

val namesRepositoryModule = module {

    // ✅ Если требуется NetworkClient — убедись, что он есть
    single<NetworkClient> {
        RetrofitNetworkClient(get(), get()) // передай context и apiService
    }

    // ✅ Репозиторий
    single<NamesRepository> {
        NamesRepositoryImpl(get()) // get() = NetworkClient
    }
}
