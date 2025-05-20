package com.example.playlistmaker.domain.usecases

import com.example.playlistmaker.domain.repository.NetworkRepository

class CheckInternetConnectionUseCase( // 📡
    private val networkRepository: NetworkRepository
) {
    fun execute(): Boolean {
        return networkRepository.isInternetAvailable()
    }
}
