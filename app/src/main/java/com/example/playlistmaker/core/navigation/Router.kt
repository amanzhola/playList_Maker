package com.example.playlistmaker.core.navigation

import androidx.fragment.app.Fragment

/**
 * Router — это входная точка для фрагментов,
 * которые хотят открыть следующий экран.
 */
interface Router {

    val navigatorHolder : NavigatorHolder

    fun openFragment(fragment: Fragment)

}