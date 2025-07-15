package com.example.playlistmaker.presentation.utils

import androidx.appcompat.app.AppCompatActivity

object BottomNavigationProvider {

    fun createHelper(
        activity: AppCompatActivity, // ⚠️ вместо BaseActivity,
        bottomViewIds: List<Int>,
        buttonIndex: Int
    ): BottomNavigationHelper {
        val buttonPairs = NavigationConfigProvider.getButtonPairs(activity)
        val navigationList = NavigationConfigProvider.getNavigationList()

        return BottomNavigationHelper(
            activity as AppCompatActivity,
            bottomViewIds,
            buttonPairs,
            navigationList,
            buttonIndex
        )
    }
}
