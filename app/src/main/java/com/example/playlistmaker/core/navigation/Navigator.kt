package com.example.playlistmaker.core.navigation

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager

/**
 * Navigator — сущность для работы с FragmentManager.
 */
interface Navigator {

    val fragmentContainerViewId: Int
    val fragmentManager: FragmentManager

    fun openFragment(fragment: Fragment)

}