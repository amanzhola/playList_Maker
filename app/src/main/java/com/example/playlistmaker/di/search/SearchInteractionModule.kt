package com.example.playlistmaker.di.search

import com.example.playlistmaker.domain.api.base.SearchHistoryInteraction
import com.example.playlistmaker.domain.api.search.AudioInteraction
import com.example.playlistmaker.domain.impl.base.SearchHistoryInteractionImpl
import com.example.playlistmaker.domain.impl.search.AudioInteractionImpl
import org.koin.dsl.module

val searchInteractionModule = module { // 🔍 from 🏠 🛠️ 🎧 ☁️ 🎥

    // Domain // Интерфейсы взаимодействия // ✅ Search history ⬅️ 🎶 📜 🎵
    single<SearchHistoryInteraction> { SearchHistoryInteractionImpl(get()) }

    // AudioInteraction // ✅ Audio interaction
    single<AudioInteraction> { AudioInteractionImpl(get(), get()) }
}
