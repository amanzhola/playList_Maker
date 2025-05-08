package com.example.playlistmaker.ui.weather

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.playlistmaker.R
import com.example.playlistmaker.domain.models.ForecastLocation

class LocationViewHolder(parent: ViewGroup) :
    RecyclerView.ViewHolder(
        LayoutInflater.from(parent.context)
            .inflate(R.layout.location_item, parent, false)) {

    var name: TextView = itemView.findViewById(R.id.locationName)

    @SuppressLint("SetTextI18n")
    fun bind(location: ForecastLocation) {
        name.text = "${location.name} (${location.country})"
    }
}