package com.example.playlistmaker.data.dto.weather

import com.google.gson.annotations.SerializedName

class ForecaAuthResponse(@SerializedName("access_token") val token: String)