package com.example.playlistmaker.domain.api

import com.example.playlistmaker.domain.models.NavigationTarget

interface NavigationUseCase {
    fun getButtonPairs(): List<Pair<String, Int>>
    fun getNavigationTargets(): List<NavigationTarget>
}
