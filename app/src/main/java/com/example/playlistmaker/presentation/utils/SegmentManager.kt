package com.example.playlistmaker.presentation.utils

import android.content.Context
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.playlistmaker.BaseActivity
import com.example.playlistmaker.R
import com.example.playlistmaker.ui.audio.SearchFragment
import com.example.playlistmaker.ui.audioPosters.ExtraOptionFragment
import com.example.playlistmaker.ui.movie.SearchMovie
import com.example.playlistmaker.ui.settings.SettingsFragment

class SegmentManager(
    private val context: Context,
    private val colorApplierHelper: ColorApplierHelper,
    private val colorPersistenceHelper: ColorPersistenceHelper,
    private val isMainActivity: Boolean,
    private val isDarkThemeEnabled: () -> Boolean,
    private val shareApp: () -> Unit,
    private val writeToSupport: () -> Unit,
    private val openAgreement: () -> Unit,
    private val shareTrackHistoryFromViewModel: () -> Unit,
    private val shareSingleTrack: () -> Unit,
    private val recreate: () -> Unit,
    private val colorManager: ColorManager,
    private val changeLanguage: () -> Unit
) {

    fun onSegmentClicked(segmentIndex: Int, isChangedState: Boolean, currentActivity: AppCompatActivity)
    {
        val randomColor = ColorHelper.getNextColor(context)
        val currentFragment = (currentActivity as? BaseActivity)?.getCurrentFragment()
        val isMainFragment = currentFragment?.toScreenType() == ScreenType.MAIN_FRAGMENT

        if (isChangedState) {

            when (segmentIndex) {
                4 -> if (!isMainFragment) changeLanguage()
                5 -> if (isMainFragment) changeLanguage() else colorManager.clearAllColors()
            }

            if (segmentIndex != 5) {
                // toolbar save and apply background color
                val scope = (currentActivity as? BaseActivity)?.getCurrentScreenKey()
                    ?: (currentActivity as BaseActivity).activityScope()

                colorPersistenceHelper.save(scope, segmentIndex, randomColor)
                colorApplierHelper.apply(segmentIndex, randomColor)
            }
        } else {
            when (segmentIndex) {
                0 -> { // Toggle theme
                    ThemeLanguageHelper.toggleTheme()

                    // применяем тему мгновенно // for activity and fragment via BaseActivity
                    (currentActivity as? AppCompatActivity)?.delegate?.applyDayNight() // ⚡ применить Day/Night к Activity
                    (currentActivity as? BaseActivity)?.applyToolbarThemeColors()  // ⚡ перекрасить тулбар под логику

                    if (currentFragment is SettingsFragment) {
                        currentFragment.syncThemeSwitchState()
                    }
                }
                1 -> {
                    val fragment = (currentActivity as? BaseActivity)?.getCurrentFragment()
                    when {
                        fragment is ExtraOptionFragment -> shareSingleTrack()
                        currentActivity is SearchMovie -> {
                            AlertDialog.Builder(currentActivity)
                                .setTitle(currentActivity.getString(R.string.share_movie_question))
                                .setMessage(currentActivity.getString(R.string.instruction_movie))
                                .setPositiveButton(currentActivity.getString(R.string.ok)) { dialog, _ -> dialog.dismiss() }
                                .show()
                        }
                        fragment is SearchFragment -> fragment.shareTrackHistoryFromViewModel()
                        else -> shareApp()
                    }
                }

                2 -> writeToSupport()
                3 -> openAgreement()
                4 -> if (isMainFragment) colorManager.clearAllColors() else (context as? BaseActivity)?.onSegment4Clicked()
                5 -> changeLanguage()
            }
        }
    }

    fun applySavedColors() {
        colorManager.applySavedColors()
    }
}