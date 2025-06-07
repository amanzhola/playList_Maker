package com.example.playlistmaker.di.extraOption

import android.app.Activity
import com.example.playlistmaker.data.repository.base.AudioSingleTrackImpl
import com.example.playlistmaker.data.repository.base.AudioTracksImpl
import com.example.playlistmaker.data.repository.player.AudioPlayerInteractionImpl
import com.example.playlistmaker.domain.api.player.AudioPlayerInteraction
import com.example.playlistmaker.domain.repository.base.AudioSingleTrackShare
import com.example.playlistmaker.domain.repository.base.AudioTracksShare
import org.koin.core.parameter.parametersOf
import org.koin.dsl.module

val extraOptionInteractionModule = module { // + from 🏠 🔍 🛠️ 🎧 ☁️ 🎥

    // Аудио плеер // for ExtraOption viewModel require  🎶 ↔️ 🎵 + use by TrackPreviewActivity (viewModel)
    single<AudioPlayerInteraction> { AudioPlayerInteractionImpl() }

    // Сервис шаринга одного трека // for ExtraOption SingleTrackShare require  // 👨‍💻 ⬇️
    factory<AudioSingleTrackShare> { (activity: Activity) ->
        AudioSingleTrackImpl(
            activity = activity,
            shareService = get { parametersOf(activity) }
        )
    }

    // Сервис шаринга списка треков  // TrackShareService  // 👨‍💻 🔝
    factory<AudioTracksShare> { (activity: Activity) ->
        AudioTracksImpl(activity)
    }
}
