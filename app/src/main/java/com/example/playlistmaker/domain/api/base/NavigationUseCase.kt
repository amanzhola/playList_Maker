package com.example.playlistmaker.domain.api.base

import com.example.playlistmaker.domain.models.base.NavigationTarget

interface NavigationUseCase {
    fun getButtonPairs(): List<Pair<String, Int>>
    fun getNavigationTargets(): List<NavigationTarget>
}
