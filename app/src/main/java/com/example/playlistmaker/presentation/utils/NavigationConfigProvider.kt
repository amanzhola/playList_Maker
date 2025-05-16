package com.example.playlistmaker.presentation.utils

import android.content.Context
import com.example.playlistmaker.MediaLibraryActivity
import com.example.playlistmaker.NavigationData
import com.example.playlistmaker.R
import com.example.playlistmaker.ui.settings.SettingsActivity
import com.example.playlistmaker.ui.audio.SearchActivity
import com.example.playlistmaker.ui.audioPosters.ExtraOption
import com.example.playlistmaker.ui.movie.SearchMovie
import com.example.playlistmaker.ui.weather.SearchWeather

object NavigationConfigProvider {

    fun getButtonPairs(context: Context): List<Pair<String, Int>> {
        return listOf(
            Pair(context.getString(R.string.search), R.drawable.search_icon),
            Pair(context.getString(R.string.media), R.drawable.media_icon),
            Pair(context.getString(R.string.settings), R.drawable.settings_icon),
            Pair(context.getString(R.string.movie), R.drawable.movies_icon),
            Pair(context.getString(R.string.weather), R.drawable.weather_icon),
            Pair(context.getString(R.string.option), R.drawable.add_box_icon)
        )
    }

    fun getNavigationList(): List<NavigationData> {
        return listOf( // 🏃‍♀️
            NavigationData(SearchActivity::class.java, 0, 0),
            NavigationData(MediaLibraryActivity::class.java, 0, 0),
            NavigationData(SettingsActivity::class.java, 0, 0),
            NavigationData(SearchMovie::class.java, 0, 0),
            NavigationData(SearchWeather::class.java, 0, 0),
            NavigationData(ExtraOption::class.java, 0, 0)
        )
    }
}
