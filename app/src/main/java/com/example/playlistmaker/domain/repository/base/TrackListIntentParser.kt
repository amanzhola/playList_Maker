package com.example.playlistmaker.domain.repository.base

import android.content.Intent
import com.example.playlistmaker.domain.models.player.TrackListInputData

interface TrackListIntentParser {
    fun parse(intent: Intent): TrackListInputData?
}