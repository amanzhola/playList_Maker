package com.example.playlistmaker.core.navigation

import androidx.fragment.app.Fragment
import com.example.playlistmaker.core.navigation.Navigator


/**
 * Сущность для хранения ссылки на Navigator.
 */
interface NavigatorHolder {

    fun attachNavigator(navigator: Navigator)

    fun detachNavigator()

    fun openFragment(fragment: Fragment)

}