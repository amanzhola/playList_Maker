package com.example.playlistmaker.domain.repository.base

interface NetworkRepository { // 📡
    suspend fun isInternetAvailable(): Boolean
}