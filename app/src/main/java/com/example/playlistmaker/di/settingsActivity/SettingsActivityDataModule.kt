package com.example.playlistmaker.di.settingsActivity

import com.example.playlistmaker.data.repository.base.ThemeRepositoryImpl
import com.example.playlistmaker.domain.repository.base.ThemeRepository
import org.koin.dsl.module

val settingsActivityDataModule = module { // 🛠️ from 🏠 🔍 🎧 ☁️ 🎥

    // Тема — для SettingsActivity // 🌓 ↔️ 🌗
    single<ThemeRepository> { ThemeRepositoryImpl(get()) }
}
