package com.example.playlistmaker.data.repository

import android.content.Context
import com.example.playlistmaker.domain.api.NavigationUseCase
import com.example.playlistmaker.domain.models.NavigationTarget
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

