package com.example.playlistmaker.presentation.utils

import android.view.ViewGroup
import com.example.playlistmaker.BaseActivity
import com.example.playlistmaker.R
import com.example.playlistmaker.presentation.utils.ColorHelper.changeBackgroundColor
import com.example.playlistmaker.presentation.utils.ColorHelper.changeCompoundDrawableColor
import com.example.playlistmaker.presentation.utils.ColorHelper.changeIconColor
import com.example.playlistmaker.presentation.utils.ColorHelper.changeTextColor
import com.example.playlistmaker.ui.audio.SearchFragment
import com.example.playlistmaker.roots.main.MainActivity
import com.example.playlistmaker.ui.settings.SettingsFragment

class ColorApplierHelper(
    private val activity: BaseActivity,
    private val mainLayout: ViewGroup, // changed from LinearLayout to ViewGroup
    private val toolbarHelper: ToolbarHelper
) {
    fun apply(segmentIndex: Int, color: Int) {
        when (segmentIndex) {
            0 -> {
                toolbarHelper.setTitleTextColor(color)
                if (activity !is MainActivity) {
                    toolbarHelper.setBackArrowColor(color)
                }
            }
            1 -> mainLayout.setBackgroundColor(color)

            2 -> when {
                activity is MainActivity -> {
                    mainLayout.changeTextColor(color)
                }

                activity.getCurrentFragment() is SearchFragment -> {
                    (activity.getCurrentFragment() as? SearchFragment)
                        ?.getAdapter()
                        ?.setTextColor(color)
                }

                else -> {
                    mainLayout.changeTextColor(color, R.id.toolbar)
                }
            }

            3 -> when {
                activity is MainActivity -> {
                    mainLayout.changeIconColor(
                        color,
                        listOf(
                            R.id.button1, R.id.button2, R.id.button3,
                            R.id.button4, R.id.button5, R.id.button6
                        )
                    )
                }
                activity.getCurrentFragment() is SettingsFragment -> {
                    mainLayout.changeCompoundDrawableColor(color, R.id.toolbar)
                }

                activity.getCurrentFragment() is SearchFragment -> {
                    (activity.getCurrentFragment() as? SearchFragment)?.getAdapter()?.setArrowColor(color)
                }
            }

            4 -> if (activity is MainActivity) {
                mainLayout.changeBackgroundColor(color)
            }
        }
    }
}
