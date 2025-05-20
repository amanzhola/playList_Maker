package com.example.playlistmaker.domain.repository

interface NetworkRepository { // 📡
    fun isInternetAvailable(): Boolean
}