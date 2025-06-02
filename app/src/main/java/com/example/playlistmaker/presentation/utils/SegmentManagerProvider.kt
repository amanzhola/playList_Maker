package com.example.playlistmaker.presentation.utils

import com.example.playlistmaker.BaseActivity
import com.example.playlistmaker.ui.audio.SearchActivity
import com.example.playlistmaker.ui.audioPosters.ExtraOption
import com.example.playlistmaker.ui.main.MainActivity

object SegmentManagerProvider {

    fun provide(
        activity: BaseActivity,
        colorApplierHelper: ColorApplierHelper,
        colorPersistenceHelper: ColorPersistenceHelper,
        colorManager: ColorManager
    ): SegmentManager {
        val isMainActivity = activity is MainActivity

        return SegmentManager(
            context = activity,
            colorApplierHelper = colorApplierHelper,
            colorPersistenceHelper = colorPersistenceHelper,
            isMainActivity = isMainActivity,
            isDarkThemeEnabled = { activity.isDarkThemeEnabled() },
            shareApp = { activity.shareApp() },
            writeToSupport = { activity.writeToSupport() },
            openAgreement = { activity.openAgreement() },
            shareTrackHistoryFromViewModel = {
                if (activity is SearchActivity) activity.shareTrackHistoryFromViewModel()
            },
            shareSingleTrack = {
                if (activity is ExtraOption) activity.shareSingleTrack()
            },
            recreate = { activity.recreate() },
            colorManager = colorManager, // Передаем созданный ColorManager
            changeLanguage = { activity.changeLanguage() }
        )
    }
}