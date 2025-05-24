package com.example.playlistmaker.domain.models.weather

import android.os.Parcel
import android.os.Parcelable

data class ForecastLocation(
    val id: Int,
    val name: String,
    val country: String
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readString() ?: "",
        parcel.readString() ?: ""
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeString(name)
        parcel.writeString(country)
    }

    override fun describeContents() = 0

    companion object CREATOR : Parcelable.Creator<ForecastLocation> {
        override fun createFromParcel(parcel: Parcel) = ForecastLocation(parcel)
        override fun newArray(size: Int) = arrayOfNulls<ForecastLocation?>(size)
    }
}