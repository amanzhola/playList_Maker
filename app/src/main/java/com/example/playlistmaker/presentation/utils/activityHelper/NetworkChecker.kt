//package com.example.playlistmaker.presentation.utils.activityHelper
//
//import com.example.playlistmaker.data.network.base.ApiService
//import com.example.playlistmaker.data.network.base.RetrofitInstance
//
//class NetworkChecker( // 📡
//    private val apiService: ApiService = RetrofitInstance.api
//) {
//    fun isInternetAvailable(): Boolean {
//        return try {
//            val response = apiService.checkInternetConnection().execute()
//            response.isSuccessful
//        } catch (e: Exception) {
//            false
//        }
//    }
//}