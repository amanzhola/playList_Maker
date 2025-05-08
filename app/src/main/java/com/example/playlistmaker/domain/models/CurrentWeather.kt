package com.example.playlistmaker.domain.models

import android.os.Parcel
import android.os.Parcelable

data class CurrentWeather(
    val temperature: Float,
    val feelsLikeTemp: Float
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readFloat(),
        parcel.readFloat()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeFloat(temperature)
        parcel.writeFloat(feelsLikeTemp)
    }

    override fun describeContents() = 0

    companion object CREATOR : Parcelable.Creator<CurrentWeather> {
        override fun createFromParcel(parcel: Parcel) = CurrentWeather(parcel)
        override fun newArray(size: Int) = arrayOfNulls<CurrentWeather?>(size)
    }
}
