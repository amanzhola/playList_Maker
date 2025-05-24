package com.example.playlistmaker.domain.usecases.base

import com.example.playlistmaker.domain.repository.base.NetworkRepository

class CheckInternetConnectionUseCase( // 📡
    private val networkRepository: NetworkRepository
) {
    fun execute(): Boolean {
        return networkRepository.isInternetAvailable()
    }
}
