package com.example.playlistmaker.domain.api.base

interface NetworkStatusChecker {
    fun isNetworkAvailable(): Boolean
}