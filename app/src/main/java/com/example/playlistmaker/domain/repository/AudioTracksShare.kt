package com.example.playlistmaker.domain.repository

import com.example.playlistmaker.R
import com.example.playlistmaker.domain.models.Track

interface AudioTracksShare {
    fun shareTracks(
        tracks: List<Track>,
        messageResId: Int = R.string.track_share,
        emptyMessageResId: Int = R.string.track_story_empty,
        fileName: String = "track_history.json"
    )
}