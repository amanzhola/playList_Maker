package com.example.playlistmaker.domain.api

interface NetworkStatusChecker {
    fun isNetworkAvailable(): Boolean
}