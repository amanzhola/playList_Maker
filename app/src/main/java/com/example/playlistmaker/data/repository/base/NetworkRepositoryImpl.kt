package com.example.playlistmaker.data.repository.base

import com.example.playlistmaker.data.network.base.ApiService
import com.example.playlistmaker.domain.api.base.NetworkStatusChecker
import com.example.playlistmaker.domain.repository.base.NetworkRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout

class NetworkRepositoryImpl(
    private val apiService: ApiService,
    private val networkStatusChecker: NetworkStatusChecker
) : NetworkRepository {

    override suspend fun isInternetAvailable(): Boolean {
        // 1️⃣ Быстрая локальная проверка (мгновенно)
        if (!networkStatusChecker.isNetworkAvailable()) return false

        // 2️⃣ Лёгкий асинхронный health-check
        return try {
            withContext(Dispatchers.IO) {
                withTimeout(1500) {
                    apiService.health().isSuccessful
                }
            }
        } catch (_: Exception) {
            false
        }
    }
}
