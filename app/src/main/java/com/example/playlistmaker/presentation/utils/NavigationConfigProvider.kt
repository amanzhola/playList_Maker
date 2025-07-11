package com.example.playlistmaker.presentation.utils

import android.content.Context
import com.example.playlistmaker.NavigationData
import com.example.playlistmaker.R
import com.example.playlistmaker.ui.audioPosters.ExtraOption
import com.example.playlistmaker.roots.main.MainActivity
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
        return listOf(
            NavigationData.ActivityData(
                activityClass = MainActivity::class.java,
                enterAnim = 0,
                exitAnim = 0,
                buttonIndex = 0 // 🔹 Search
            ),
            NavigationData.ActivityData(
                activityClass = MainActivity::class.java,
                enterAnim = 0,
                exitAnim = 0,
                buttonIndex = 1 // 🔹 Media
            ),
            NavigationData.ActivityData(
                activityClass = MainActivity::class.java,
                enterAnim = 0,
                exitAnim = 0,
                buttonIndex = 2 // 🔹 Settings
            ),
            NavigationData.ActivityData(
                activityClass = SearchMovie::class.java,
                enterAnim = 0,
                exitAnim = 0,
                buttonIndex = 3
            ),
            NavigationData.ActivityData(
                activityClass = SearchWeather::class.java,
                enterAnim = 0,
                exitAnim = 0,
                buttonIndex = 4
            ),
            NavigationData.ActivityData(
                activityClass = ExtraOption::class.java,
                enterAnim = 0,
                exitAnim = 0,
                buttonIndex = 5
            )
        )
    }
}
