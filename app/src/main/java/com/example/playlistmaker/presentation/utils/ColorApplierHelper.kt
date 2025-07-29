package com.example.playlistmaker.presentation.utils

import android.view.ViewGroup
import com.example.playlistmaker.BaseActivity
import com.example.playlistmaker.R
import com.example.playlistmaker.presentation.utils.ColorHelper.changeBackgroundColor
import com.example.playlistmaker.presentation.utils.ColorHelper.changeCompoundDrawableColor
import com.example.playlistmaker.presentation.utils.ColorHelper.changeIconColor
import com.example.playlistmaker.presentation.utils.ColorHelper.changeTextColor
import com.example.playlistmaker.ui.audio.SearchFragment
import com.example.playlistmaker.ui.settings.SettingsFragment

class ColorApplierHelper(
    private val activity: BaseActivity,
    private val mainLayout: ViewGroup, // changed from LinearLayout to ViewGroup
    private val toolbarHelper: ToolbarHelper
) {
    fun apply(segmentIndex: Int, color: Int) {

        val currentFragment = activity.getCurrentFragment()
        val isMainFragment = currentFragment?.toScreenType() == ScreenType.MAIN_FRAGMENT

        when (segmentIndex) {
            0 -> {
                toolbarHelper.setTitleTextColor(color)
                if (!isMainFragment) {
                    toolbarHelper.setBackArrowColor(color)
                }
            }
            1 -> {
                mainLayout.setBackgroundColor(color)
                if (!isMainFragment) {
                    val fragmentRoot = currentFragment?.view as? ViewGroup
                    fragmentRoot?.applyBackgroundRecursively(color)
                }
            }
            2 -> when {
                isMainFragment -> mainLayout.changeTextColor(color)
                currentFragment is SearchFragment -> {
                    currentFragment.getAdapter()?.setTextColor(color)
                }
                else -> {
                    mainLayout.changeTextColor(color, R.id.toolbar)
                }
            }
            3 -> when {
                isMainFragment -> {
                    mainLayout.changeIconColor(
                        color,
                        listOf(R.id.button1, R.id.button2, R.id.button3, R.id.button4, R.id.button5, R.id.button6)
                    )
                }
                currentFragment is SettingsFragment -> {
                    mainLayout.changeCompoundDrawableColor(color, R.id.toolbar)
                }
                currentFragment is SearchFragment -> {
                    currentFragment.getAdapter()?.setArrowColor(color)
                }
            }
            4 -> if (isMainFragment) {
                mainLayout.changeBackgroundColor(color)
            }
        }
    }

    private fun ViewGroup.applyBackgroundRecursively(color: Int) {
        this.setBackgroundColor(color)
        for (i in 0 until childCount) {
            val child = getChildAt(i)
            if (child is ViewGroup) {
                child.applyBackgroundRecursively(color)
            } else {
                child.setBackgroundColor(color)
            }
        }
    }

}
