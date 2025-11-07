package com.example.playlistmaker.presentation.utils

import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.playlistmaker.BaseActivity
import com.example.playlistmaker.R
import com.example.playlistmaker.presentation.utils.ColorHelper.changeBackgroundColor
import com.example.playlistmaker.presentation.utils.ColorHelper.changeIconColor
import com.example.playlistmaker.presentation.utils.ColorHelper.changeTextColor
import com.example.playlistmaker.ui.audio.SearchFragment
import com.example.playlistmaker.ui.audioPosters.AudioPlayerFragment
import com.example.playlistmaker.ui.media.MediaLibraryFragment
import com.example.playlistmaker.ui.settings.SettingsFragment

class ColorApplierHelper(
    private val activity: BaseActivity,
    private val mainLayout: ViewGroup,
    private val toolbarHelper: ToolbarHelper
) {
    fun apply(segmentIndex: Int, color: Int) {
        val vf = activity.getVisibleScreenFragmentOrNull()
        val isMain = activity.getCurrentFragment()?.toScreenType() == ScreenType.MAIN_FRAGMENT

        val mediaParent: MediaLibraryFragment? = vf.resolveMediaParent()

        when (segmentIndex) {
            0 -> { // заголовок/стрелка — как было
                toolbarHelper.setTitleTextColor(color)
                if (!isMain) toolbarHelper.setBackArrowColor(color)
            }

            1 -> { // ===== ФОН =====
                if (mediaParent != null) {
                    // для Media: тулбар только по теме, фон меняем ТОЛЬКО через состояние родителя
                    toolbarHelper.applyThemeColors()
                    mediaParent.setActiveBackground(color)   // <<< это триггерит Compose у нужной вкладки
                    return
                }

                // НЕ media: старое поведение
                val excludeIds = (vf as? BackgroundExclusionProvider)?.backgroundExclusionIds().orEmpty()
                vf?.let { frag ->
                    (frag.view as? ViewGroup)?.applyBackgroundRecursively(color, excludeIds)
                    toolbarHelper.setBackgroundColor(color)
                } ?: run {
                    mainLayout.setBackgroundColor(color)
                    toolbarHelper.setBackgroundColor(color)
                }

<<<<<<< Updated upstream
                (vf as? SettingsFragment)?.setSettingsBackgroundColor(color)
                (vf as? SearchFragment )?.setListBackGroundColor(color)
                (vf as? AudioPlayerFragment )?.setAudioBackgroundColor(color)
            }

            2 -> { // ===== ТЕКСТ =====
                if (mediaParent != null) {
                    mediaParent.setActiveTextColor(color)    // только активный таб
                } else {
                    (vf as? SearchFragment )?.setListTextColor(color)
                    (vf as? SettingsFragment)?.setSettingsTextColor(color)
                    (vf as? AudioPlayerFragment )?.setAudioTextColor(color)
                    if (vf == null) mainLayout.changeTextColor(color, R.id.toolbar)
                    else if (isMain) mainLayout.changeTextColor(color)
                }
            }

            3 -> { // ===== ИКОНКИ =====
                if (mediaParent != null) {
                    mediaParent.setActiveIconColor(color)    // только Favourite таб имеет иконки
                } else {
                    (vf as? SearchFragment )?.setListArrowColor(color)
                    (vf as? SettingsFragment)?.setSettingsIconColor(color)
                    (vf as? AudioPlayerFragment )?.setAudioIconColor(color)
                    if (isMain) {
                        mainLayout.changeIconColor(
                            color,
                            listOf(R.id.button1, R.id.button2, R.id.button3, R.id.button4, R.id.button5, R.id.button6)
                        )
                    }
=======
                if (visibleFragment is SearchFragment) { // больше нет RecyclerView-адаптера → зовём метод фрагмента
                    visibleFragment.setListBackGroundColor(color)
                }
            }
            2 -> when {
                isMainFragment -> mainLayout.changeTextColor(color)
                currentFragment is SearchFragment -> {
                    // больше нет RecyclerView-адаптера → зовём метод фрагмента
                    currentFragment.setListTextColor(color)
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
                    // больше нет RecyclerView-адаптера → зовём метод фрагмента
                    currentFragment.setListArrowColor(color)
>>>>>>> Stashed changes
                }
            }

            4 -> { // фон “главной”
                if (isMain) mainLayout.changeBackgroundColor(color)
            }
        }
    }
}

// рекурсивная заливка — без изменений
private fun ViewGroup.applyBackgroundRecursively(
    color: Int,
    excludeIds: Set<Int> = emptySet()
) {
    if (this.id in excludeIds) return
    setBackgroundColor(color)
    for (i in 0 until childCount) {
        val ch = getChildAt(i)
        if (ch.id in excludeIds) continue
        if (ch is ViewGroup) ch.applyBackgroundRecursively(color, excludeIds)
        else ch.setBackgroundColor(color)
    }
}

fun Fragment.resetOwnBackgroundTo(color: Int, excludeIds: Set<Int> = emptySet()) {
    (view as? ViewGroup)?.applyBackgroundRecursively(color, excludeIds)
}

private tailrec fun Fragment?.resolveMediaParent(): MediaLibraryFragment? = when (this) {
    null -> null
    is MediaLibraryFragment -> this
    else -> this.parentFragment.resolveMediaParent()
}
