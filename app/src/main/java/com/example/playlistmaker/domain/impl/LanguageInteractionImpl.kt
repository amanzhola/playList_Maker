package com.example.playlistmaker.domain.impl

import com.example.playlistmaker.domain.api.LanguageInteraction
import com.example.playlistmaker.domain.repository.LanguageRepository

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
