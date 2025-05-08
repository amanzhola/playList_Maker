package com.example.playlistmaker.data.network.response

data class CustomNetworkResponse<T>(
    val isSuccessful: Boolean,
    val code: Int,
    val body: T?,
    val errorBody: String?
)