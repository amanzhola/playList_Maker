package com.example.playlistmaker.presentation.utils

import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
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

            1 -> { // toolbar save and apply background color
                val visibleFragment = activity.getVisibleScreenFragmentOrNull()

                // исключения: спросим у фрагмента (если поддерживает)
                val excludeIds = (visibleFragment as? BackgroundExclusionProvider)?.backgroundExclusionIds().orEmpty()

                visibleFragment?.let { frag ->
                    // есть ребёнок → красим ТОЛЬКО его root // есть фрагмент/таба → красим ТОЛЬКО его root И общий тулбар (mainLayout НЕ трогаем)
                    (frag.view as? ViewGroup)?.applyBackgroundRecursively(color, excludeIds)
                    toolbarHelper.setBackgroundColor(color)
                } ?: run {
                    // ребёнка нет → “чистая” активити // «чистая» активити → красим корневой layout И общий тулбар
                    mainLayout.setBackgroundColor(color)
                    toolbarHelper.setBackgroundColor(color)
                }
            }
            2 -> when {
                isMainFragment -> mainLayout.changeTextColor(color)
                currentFragment is SearchFragment -> {
                    currentFragment.getAdapter().setTextColor(color)
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
                    currentFragment.getAdapter().setArrowColor(color)
                }
            }
            4 -> if (isMainFragment) {
                mainLayout.changeBackgroundColor(color)
            }
        }
    }

    // toolbar save and apply background color
    private fun ViewGroup.applyBackgroundRecursively(
        color: Int,
        excludeIds: Set<Int> = emptySet()
    ) {
        if (this.id in excludeIds) return
        this.setBackgroundColor(color)
        for (i in 0 until childCount) {
            val child = getChildAt(i)
            if (child.id in excludeIds) continue
            if (child is ViewGroup) {
                child.applyBackgroundRecursively(color, excludeIds)
            } else {
                child.setBackgroundColor(color)
            }
        }
    }

    // Позволяет дочернему фрагменту быстро сбросить свой фон в дефолт
    fun Fragment.resetOwnBackgroundTo(color: Int, excludeIds: Set<Int> = emptySet()) {
        (view as? ViewGroup)?.applyBackgroundRecursively(color, excludeIds)
    }
}

fun View?.resetBackgroundRecursively(color: Int, excludeIds: Set<Int> = emptySet()) {
    val vg = this as? ViewGroup ?: return
    if (vg.id !in excludeIds) vg.setBackgroundColor(color)
    for (i in 0 until vg.childCount) {
        val ch = vg.getChildAt(i)
        if (ch.id in excludeIds) continue
        if (ch is ViewGroup) ch.resetBackgroundRecursively(color, excludeIds)
        else ch.setBackgroundColor(color)
    }
}

// Если хочешь вызывать именно на ViewGroup — можно и такой синоним оставить:
fun ViewGroup.applyBackgroundRecursively(
    color: Int,
    excludeIds: Set<Int> = emptySet()
) {
    this.resetBackgroundRecursively(color, excludeIds)
}
