package com.example.playlistmaker.data.repository.base

import android.content.Context
import com.example.playlistmaker.NavigationData
import com.example.playlistmaker.domain.api.base.NavigationUseCase
import com.example.playlistmaker.domain.models.base.NavigationTarget
import com.example.playlistmaker.presentation.utils.NavigationConfigProvider

class NavigationUseCaseImpl(private val context: Context) : NavigationUseCase {

    override fun getButtonPairs(): List<Pair<String, Int>> {
        return NavigationConfigProvider.getButtonPairs(context)
    }

    override fun getNavigationTargets(): List<NavigationTarget> {
        return NavigationConfigProvider.getNavigationList().map { data ->
            when (data) {
                is NavigationData.ActivityData -> NavigationTarget.ActivityTarget(
                    activityClass = data.activityClass,
                    enterAnim = data.enterAnim,
                    exitAnim = data.exitAnim
                )

//                is NavigationData.FragmentData -> NavigationTarget.FragmentTarget(
//                    fragmentProvider = data.fragmentProvider,
//                    tag = data.tag,
//                    enterAnim = data.enterAnim,
//                    exitAnim = data.exitAnim
//                )

                is NavigationData.FragmentData -> NavigationTarget.FragmentTarget(
                    destinationId = data.destinationId,
                    args = data.args,
                    enterAnim = data.enterAnim,
                    exitAnim = data.exitAnim
                )

            }
        }
    }

}
