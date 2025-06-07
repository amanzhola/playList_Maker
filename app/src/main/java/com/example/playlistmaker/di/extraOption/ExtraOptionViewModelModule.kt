package com.example.playlistmaker.di.extraOption

import com.example.playlistmaker.presentation.searchPostersViewModels.ExtraOptionViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val extraOptionViewModelModule = module { // + from 🏠 🔍 🛠️ 🎧 ☁️ 🎥

    // get() → AudioPlayerInteraction
    viewModel { ExtraOptionViewModel(get()) }
}
