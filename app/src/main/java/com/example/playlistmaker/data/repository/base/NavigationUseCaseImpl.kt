package com.example.playlistmaker.data.repository.base

import android.content.Context
import com.example.playlistmaker.domain.api.base.NavigationUseCase
import com.example.playlistmaker.domain.models.base.NavigationTarget
import com.example.playlistmaker.presentation.utils.NavigationConfigProvider

class NavigationUseCaseImpl(private val context: Context) : NavigationUseCase {
    override fun getButtonPairs(): List<Pair<String, Int>> {
        return NavigationConfigProvider.getButtonPairs(context)
    }

    override fun getNavigationTargets(): List<NavigationTarget> {
        return NavigationConfigProvider.getNavigationList().map {
            NavigationTarget(activityClass = it.activityClass)
        }
    }
}

