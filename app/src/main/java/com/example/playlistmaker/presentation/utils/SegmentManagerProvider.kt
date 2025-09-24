package com.example.playlistmaker.presentation.utils

import com.example.playlistmaker.BaseActivity
import com.example.playlistmaker.ui.audio.SearchFragment
import com.example.playlistmaker.ui.audioPosters.AudioPlayerFragment

object SegmentManagerProvider {

        fun provide(
            activity: BaseActivity,
            colorApplierHelper: ColorApplierHelper,
            colorPersistenceHelper: ColorPersistenceHelper,
            colorManager: ColorManager
        ): SegmentManager {
            val currentFragment = activity.getCurrentFragment()
            val isMainFragment = currentFragment?.toScreenType() == ScreenType.MAIN_FRAGMENT


            return SegmentManager(
                context = activity,
                colorApplierHelper = colorApplierHelper,
                colorPersistenceHelper = colorPersistenceHelper,
                isMainActivity = isMainFragment, // 👈 меняем тут
                isDarkThemeEnabled = { activity.isDarkThemeEnabled() },
                shareApp = { activity.shareApp() },
                writeToSupport = { activity.writeToSupport() },
                openAgreement = { activity.openAgreement() },
                shareTrackHistoryFromViewModel = {
                    val fragment = activity.getCurrentFragment()
                    if (fragment is SearchFragment) {
                        fragment.shareTrackHistoryFromViewModel()
                    }
                },

                        shareSingleTrack = {
                    val fragment = activity.getCurrentFragment()
                    if (fragment is AudioPlayerFragment) {
                        fragment.shareSingleTrack()
                    }
                },
                recreate = { activity.recreate() },
                colorManager = colorManager, // Передаем созданный ColorManager
                changeLanguage = { activity.changeLanguage() }
            )
        }
    }