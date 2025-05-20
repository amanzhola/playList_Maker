package com.example.playlistmaker.data.repository

import com.example.playlistmaker.data.network.ApiService
import com.example.playlistmaker.domain.repository.NetworkRepository

class NetworkRepositoryImpl( // 📡
    private val apiService: ApiService
) : NetworkRepository {
    override fun isInternetAvailable(): Boolean {
        return try {
            val response = apiService.checkInternetConnection().execute()
            response.isSuccessful
        } catch (e: Exception) {
            false
        }
    }
}
