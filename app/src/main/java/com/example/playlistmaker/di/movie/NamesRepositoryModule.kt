package com.example.playlistmaker.di.movie

import com.example.playlistmaker.data.network.wiki.UserAgentInterceptor
import com.example.playlistmaker.data.network.wiki.WikiApi
import com.example.playlistmaker.data.repository.moviePersons.NamesRepositoryImpl
import com.example.playlistmaker.domain.api.moviePersons.NamesRepository
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

val namesRepositoryModule = module {

    single { HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY } }
    single { UserAgentInterceptor("PlaylistMaker/1.0 (https://your.site; support@your.site)") }

    single {
        OkHttpClient.Builder()
            .addInterceptor(get<HttpLoggingInterceptor>())
            .addInterceptor(get<UserAgentInterceptor>())
            .build()
    }

    single(named("retrofit_ru")) {
        Retrofit.Builder()
            .baseUrl("https://ru.wikipedia.org/")
            .client(get())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    single<WikiApi>(named("api_ru")) { get<Retrofit>(named("retrofit_ru")).create(WikiApi::class.java) }

    single(named("retrofit_en")) {
        Retrofit.Builder()
            .baseUrl("https://en.wikipedia.org/")
            .client(get())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    single<WikiApi>(named("api_en")) { get<Retrofit>(named("retrofit_en")).create(WikiApi::class.java) }

    single<NamesRepository> { NamesRepositoryImpl(get(named("api_ru")), get(named("api_en"))) }
}
