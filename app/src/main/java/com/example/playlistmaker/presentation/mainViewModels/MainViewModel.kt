package com.example.playlistmaker.presentation.mainViewModels

import androidx.lifecycle.ViewModel
import com.example.playlistmaker.domain.api.LanguageInteraction
import com.example.playlistmaker.domain.api.NavigationUseCase
import com.example.playlistmaker.ui.model_main.ButtonUiModel


class MainViewModel(
    private val navigationUseCase: NavigationUseCase,
    private val languageInteraction: LanguageInteraction
) : ViewModel() {

    fun getButtonUiModel(index: Int): ButtonUiModel? {
        val buttonPair = navigationUseCase.getButtonPairs().getOrNull(index) ?: return null
        val localizedText = buttonPair.first
        val icon = if (languageInteraction.getLanguage() == "ru") buttonPair.second else null
        return ButtonUiModel(localizedText, icon)
    }

    fun getActivityClass(index: Int): Class<*>? {
        return navigationUseCase.getNavigationTargets().getOrNull(index)?.activityClass
    }
}
