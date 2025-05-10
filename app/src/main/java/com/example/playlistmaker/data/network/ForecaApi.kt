package com.example.playlistmaker.data.network

import com.example.playlistmaker.data.dto.ForecastResponse
import com.example.playlistmaker.data.dto.LocationsResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path

interface ForecaApi {
//    @POST("/authorize/token?expire_hours=-1")
//    suspend fun authenticate(@Body request: ForecaAuthRequest): Response<ForecaAuthResponse>

    @GET("/api/v1/location/search/{query}")
    suspend fun getLocations(@Header("Authorization")
                             token: String, @Path("query") query: String): Response<LocationsResponse>

    @GET("/api/v1/current/{location}")
    suspend fun getForecast(@Header("Authorization")
                            token: String, @Path("location") locationId: Int): Response<ForecastResponse>
}