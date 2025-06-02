package com.example.playlistmaker.domain.repository.base

interface NetworkRepository { // 📡
    fun isInternetAvailable(): Boolean
}