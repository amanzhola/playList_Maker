package com.example.playlistmaker.presentation.utils

import com.example.playlistmaker.BaseActivity

object BottomNavigationProvider {

    fun createHelper(
        activity: BaseActivity,
        bottomViewIds: List<Int>,
        buttonIndex: Int
    ): BottomNavigationHelper {
        val buttonPairs = NavigationConfigProvider.getButtonPairs(activity)
        val navigationList = NavigationConfigProvider.getNavigationList()

        return BottomNavigationHelper(
            activity,
            bottomViewIds,
            buttonPairs,
            navigationList,
            buttonIndex
        )
    }
}
