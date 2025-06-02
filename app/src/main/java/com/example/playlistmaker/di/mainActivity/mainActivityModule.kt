package com.example.playlistmaker.di.mainActivity

import com.example.playlistmaker.data.repository.base.NavigationUseCaseImpl
import com.example.playlistmaker.domain.api.base.NavigationUseCase
import com.example.playlistmaker.presentation.mainViewModels.MainViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val mainActivityModule = module { // 🏠 from  🔍 🛠️ 🎧 ☁️ 🎥

    // NavigationUseCase 🚗 💖
    single<NavigationUseCase> { NavigationUseCaseImpl(androidContext()) }

    viewModel { MainViewModel(get(), get()) }
}
