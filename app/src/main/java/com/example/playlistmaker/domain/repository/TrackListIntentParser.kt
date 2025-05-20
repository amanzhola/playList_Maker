package com.example.playlistmaker.domain.repository

import android.content.Intent
import com.example.playlistmaker.domain.models.TrackListInputData

interface TrackListIntentParser {
    fun parse(intent: Intent): TrackListInputData?
}