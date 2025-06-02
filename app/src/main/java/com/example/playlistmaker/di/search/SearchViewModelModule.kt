package com.example.playlistmaker.di.search

import com.example.playlistmaker.presentation.searchViewModels.SearchViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val searchViewModelModule = module {// 🔍 from 🏠 🛠️ 🎧 ☁️ 🎥

    // Presentation // ViewModel // ✅ ViewModel
    viewModel { SearchViewModel(get(), get()) }
}
