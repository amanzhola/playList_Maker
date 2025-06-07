package com.example.playlistmaker.di.settingsActivity

import com.example.playlistmaker.domain.api.base.ThemeInteraction
import com.example.playlistmaker.domain.impl.base.ThemeInteractionImpl
import org.koin.dsl.module

val settingsActivityInteractionModule = module { // 🛠️ from 🏠 🔍 🎧 ☁️ 🎥

    // Theme — для SettingsActivity // 🌓 ↔️ 🌗
    single<ThemeInteraction> { ThemeInteractionImpl(get()) }
}
