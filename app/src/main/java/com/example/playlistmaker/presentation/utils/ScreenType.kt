package com.example.playlistmaker.presentation.utils

import androidx.fragment.app.Fragment
import com.example.playlistmaker.ui.audio.SearchFragment
import com.example.playlistmaker.ui.audioPosters.AudioPlayerFragment
import com.example.playlistmaker.ui.main.MainFragment
import com.example.playlistmaker.ui.media.MediaLibraryFragment
import com.example.playlistmaker.ui.settings.SettingsFragment

enum class ScreenType {
    MAIN_FRAGMENT,
    SEARCH_FRAGMENT,
    AUDIO_FRAGMENT,
    MEDIA_LIBRARY_FRAGMENT,
    SETTINGS_FRAGMENT,
    SEARCH_MOVIE_ACTIVITY,
    SEARCH_WEATHER_ACTIVITY,
    UNKNOWN,
}

fun Fragment?.toScreenType(): ScreenType {
    return when (this) {
        is MainFragment -> ScreenType.MAIN_FRAGMENT
        is SearchFragment -> ScreenType.SEARCH_FRAGMENT
        is AudioPlayerFragment -> ScreenType.AUDIO_FRAGMENT
        is MediaLibraryFragment -> ScreenType.MEDIA_LIBRARY_FRAGMENT
        is SettingsFragment -> ScreenType.SETTINGS_FRAGMENT
        else -> ScreenType.UNKNOWN
    }
}
