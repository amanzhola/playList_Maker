package com.example.playlistmaker.data.network

import com.example.playlistmaker.data.network.response.CustomNetworkResponse
import retrofit2.Response
import java.io.IOException

// Функция, которая выполняет Retrofit call и адаптирует ответ в CustomNetworkResponse
suspend fun <T> safeApiCallAndAdapt(
    call: suspend () -> Response<T>
): CustomNetworkResponse<T> {
    return try {
        val response = call.invoke()
        CustomNetworkResponse(
            isSuccessful = response.isSuccessful,
            code = response.code(),
            body = response.body(),
            errorBody = response.errorBody()?.string()
        )
    } catch (e: IOException) {
        CustomNetworkResponse(
            isSuccessful = false,
            code = -1,
            body = null,
            errorBody = "Network error: ${e.localizedMessage}"
        )
    } catch (e: Exception) {
        CustomNetworkResponse(
            isSuccessful = false,
            code = -2,
            body = null,
            errorBody = "An unexpected error occurred: ${e.localizedMessage}"
        )
    }
}