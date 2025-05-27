package com.example.playlistmaker.domain.impl.base

import com.example.playlistmaker.domain.api.base.LanguageInteraction
import com.example.playlistmaker.domain.repository.base.LanguageRepository

class LanguageInteractionImpl(
    private val repository: LanguageRepository
) : LanguageInteraction {

    override fun toggleLanguage(): String {
        return repository.toggleLanguage()
    }

    override fun getLanguage(): String {
        return repository.getLanguage()
    }
}
