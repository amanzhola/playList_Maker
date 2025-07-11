package com.example.playlistmaker.di.navigation

import com.example.playlistmaker.core.navigation.Router
import com.example.playlistmaker.core.navigation.RouterImpl
import org.koin.dsl.module

val navigationModule = module {
    val router = RouterImpl()

    single<Router> { router }
    single { router.navigatorHolder }
}