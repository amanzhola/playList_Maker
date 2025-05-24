package com.example.playlistmaker.data.repository.base

import com.example.playlistmaker.data.network.base.ApiService
import com.example.playlistmaker.domain.repository.base.NetworkRepository

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
