package com.example.playlistmaker.presentation.utils

import android.app.AlertDialog
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import com.example.playlistmaker.R
import com.example.playlistmaker.ui.audio.SearchActivity
import com.example.playlistmaker.ui.audioPosters.ExtraOption
import com.example.playlistmaker.ui.movie.SearchMovie
import com.example.playlistmaker.ui.settings.SettingsActivity

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

        if (isChangedState) {
            when (segmentIndex) {
                4 -> if (!isMainActivity) changeLanguage()
                5 -> if (isMainActivity) changeLanguage() else colorManager.clearAllColors()
            }
            if (segmentIndex != 5) {
                colorPersistenceHelper.save(segmentIndex, randomColor)
                colorApplierHelper.apply(segmentIndex, randomColor)
            }
        } else {
            when (segmentIndex) {
                0 -> { // Toggle theme
                    ThemeLanguageHelper.toggleTheme()
                    if (currentActivity is SettingsActivity) {
                        currentActivity.syncThemeSwitchState()
                    }
                }
                1 -> {
                    when (currentActivity) {
                        is SearchActivity -> shareTrackHistoryFromViewModel()
                        is ExtraOption -> shareSingleTrack()
                        is SearchMovie ->  {
                            AlertDialog.Builder(currentActivity)
                                .setTitle(currentActivity.getString(R.string.share_movie_question))
                                .setMessage(currentActivity.getString(R.string.instruction_movie))
                                .setPositiveButton(currentActivity.getString(R.string.ok)) { dialog, _ -> dialog.dismiss() }
                                .show()
                        }
                        else -> shareApp()
                    }
                }
                2 -> writeToSupport()
                3 -> openAgreement()
                4 -> if (isMainActivity) colorManager.clearAllColors()
                5 -> changeLanguage()
            }
        }
    }

    fun applySavedColors() {
        colorManager.applySavedColors()
    }
}