package com.example.playlistmaker.di.extraOption

import android.app.Activity
import com.example.playlistmaker.data.repository.base.AudioSingleTrackImpl
import com.example.playlistmaker.data.repository.base.AudioTracksImpl
import com.example.playlistmaker.data.repository.player.AudioPlayerInteractionImpl
import com.example.playlistmaker.domain.api.base.NetworkStatusChecker
import com.example.playlistmaker.domain.api.player.AudioPlayerInteraction
import com.example.playlistmaker.domain.repository.base.AudioSingleTrackShare
import com.example.playlistmaker.domain.repository.base.AudioTracksShare
import org.koin.android.ext.koin.androidContext
import org.koin.core.parameter.parametersOf
import org.koin.dsl.module

val extraOptionInteractionModule = module { // + from 🏠 🔍 🛠️ 🎧 ☁️ 🎥

    // Аудио плеер // for ExtraOption viewModel require  🎶 ↔️ 🎵 + use by TrackPreviewActivity (viewModel)
    single<AudioPlayerInteraction> { AudioPlayerInteractionImpl() }

    // use by TrackPreviewActivity (viewModel) for future possible removing ->
    // single<AudioPlayerInteraction> { AudioPlayerInteractionImpl() } ViewModel больше не получает AudioPlayerInteraction.
    // Вместо этого Fragment биндинит MusicService и передаёт его в VM через setAudioPlayerControl(...).

    // Сервис шаринга одного трека // for ExtraOption SingleTrackShare require  // 👨‍💻 ⬇️
    factory<AudioSingleTrackShare> { (activity: Activity) ->
        AudioSingleTrackImpl(
            activity = activity,
            shareService = get { parametersOf(activity) },
            networkStatusChecker = get<NetworkStatusChecker> { parametersOf(androidContext()) }
        )
    }

    // Сервис шаринга списка треков  // TrackShareService  // 👨‍💻 🔝
    factory<AudioTracksShare> { (activity: Activity) ->
        AudioTracksImpl(
            activity = activity,
            networkStatusChecker = get<NetworkStatusChecker> { parametersOf(androidContext()) }
        )
    }
}
