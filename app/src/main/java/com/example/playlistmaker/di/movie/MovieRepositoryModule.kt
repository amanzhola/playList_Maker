package com.example.playlistmaker.di.movie

import com.example.playlistmaker.data.converters.MovieCastConverter
import com.example.playlistmaker.data.movie_db.MovieDbConvertor
import com.example.playlistmaker.data.network.movie.IMDbApi
import com.example.playlistmaker.data.network.movieDetails.IMDbApiService
import com.example.playlistmaker.data.network.movieDetails.NetworkClient
import com.example.playlistmaker.data.network.movieDetails.RetrofitNetworkClient
import com.example.playlistmaker.data.network.wiki.UserAgentInterceptor
import com.example.playlistmaker.data.network.wiki.WikiLangApi
import com.example.playlistmaker.data.repository.base.FavoritesRepositoryImpl
import com.example.playlistmaker.data.repository.movie.MoviesRepositoryImpl
import com.example.playlistmaker.data.repository.movieDetails.MoviesRepositoryImplPoster
import com.example.playlistmaker.data.translator.TextTranslatorImpl
import com.example.playlistmaker.data.translator.TranslateBatcher
import com.example.playlistmaker.data.translator.WikiTitleResolver
import com.example.playlistmaker.domain.api.movie.MoviesRepository
import com.example.playlistmaker.domain.api.movie_db.HistoryRepository
import com.example.playlistmaker.domain.api.moviesDetails.PosterMovieRepository
import com.example.playlistmaker.domain.impl.movie_db.HistoryRepositoryImpl
import com.example.playlistmaker.domain.repository.base.FavoritesRepository
import com.example.playlistmaker.domain.usecases.movie.ToggleFavoriteUseCase
import com.example.playlistmaker.domain.util.TextTranslator
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

val movieRepositoryModule = module {// 🎥 💃 🎬 // 🎥  from 🏠 🔍 🛠️ 🎧 ☁️

    // FavoritesRepository // 📥🔄 ❤️🧲🔝 🌟 (MoviesViewModel)(MoviePager)
    single<FavoritesRepository> { FavoritesRepositoryImpl(get()) }

    // ToggleFavoriteUseCase // 📥🔄 ❤️🧲🔝 🌟 (MoviesViewModel) (MoviePager)
    single { ToggleFavoriteUseCase(get()) }

    // IMDbApi // 🌐 (MoviesViewModel)
    single<IMDbApi> {
        Retrofit.Builder()
            .baseUrl("https://tv-api.com")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(IMDbApi::class.java)
    }

    // update Movies Data Base -> MoviesRepository // 🌐 (MoviesViewModel)
    single<MoviesRepository> {
        MoviesRepositoryImpl(
            apiService = get(),
            apiKey =  "k_zcuw1ytf",
            batcher = get(),
            appDatabase = get(),         // 👈 подтягиваем из dataModule
            movieDbConvertor = get(),     // 👈 подтягиваем конвертер
            wikiResolver = get()
        )
    }
    //**********************************translator low level for movie
    single(qualifier = named("enToRu")) {
        Translation.getClient(
            TranslatorOptions.Builder()
                .setSourceLanguage(TranslateLanguage.ENGLISH)
                .setTargetLanguage(TranslateLanguage.RUSSIAN)
                .build()
        )
    }

    single(qualifier = named("ruToEn")) {
        Translation.getClient(
            TranslatorOptions.Builder()
                .setSourceLanguage(TranslateLanguage.RUSSIAN)
                .setTargetLanguage(TranslateLanguage.ENGLISH)
                .build()
        )
    }

    single<TextTranslator> {
        TextTranslatorImpl(
            enToRu = get(qualifier = named("enToRu")),
            ruToEn = get(qualifier = named("ruToEn"))
        )
    }
    //**********************************^^^^
    // adds for Poster Fragments
    // 🌐 IMDbApiService
    single<IMDbApiService> {
        Retrofit.Builder()
            .baseUrl("https://tv-api.com") // или другой корректный URL
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(IMDbApiService::class.java)
    }

    // ✅ NetworkClient = RetrofitNetworkClient
    single<NetworkClient> {
        RetrofitNetworkClient(get(), get()) // IMDbApiService, Context
    }

    // Добавили фабрику для конвертера
    factory { MovieCastConverter() }

    // 🌟 PosterMovieRepository использует NetworkClient
    single<PosterMovieRepository> {
        // Добавили ещё один `get()`, чтобы количество
        // аргументов совпадало
        MoviesRepositoryImplPoster(get(), get())
    }

    // Экземпляр конвертера для MovieEntity ↔ Movie
    factory { MovieDbConvertor() }

    // Movie Data Base
    single<HistoryRepository> {
        HistoryRepositoryImpl(get(), get())
    }
    //**********************************wiki for title search movie
    single { HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY } }
    single { UserAgentInterceptor() }
    single(named("okhttp_wiki")) {
        OkHttpClient.Builder()
            .addInterceptor(get<UserAgentInterceptor>())
            .addInterceptor(get<HttpLoggingInterceptor>())
            .build()
    }
    single(named("retrofit_ru_wiki")) {
        Retrofit.Builder()
            .baseUrl("https://ru.wikipedia.org/")
            .client(get(named("okhttp_wiki")))
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    single<WikiLangApi> { get<Retrofit>(named("retrofit_ru_wiki")).create(WikiLangApi::class.java) }
    single { WikiTitleResolver(get()) }

    //**********************************translator high level use above
    single { TranslateBatcher(translator = get(), parallelism = 6, cacheLimit = 800) }
}
