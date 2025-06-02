package com.example.playlistmaker.di.settingsActivity

import com.example.playlistmaker.presentation.settingsViewModels.SettingsViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val settingsActivityViewModelModule = module { // 🛠️ from 🏠 🔍 🎧 ☁️ 🎥

    // Theme — для SettingsActivity // 🌓 ↔️ 🌗
    viewModel { SettingsViewModel(get()) }
}
