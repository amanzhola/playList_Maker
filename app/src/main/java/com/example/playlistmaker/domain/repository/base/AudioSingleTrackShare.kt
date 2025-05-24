package com.example.playlistmaker.domain.repository.base

import com.example.playlistmaker.R
import com.example.playlistmaker.domain.models.search.Track

interface AudioSingleTrackShare {
    fun shareTrackOrNotify(
        track: Track?,
        messageResId: Int = R.string.track_share,
        emptyMessageResId: Int = R.string.empty_track,
        fileName: String = "single_track.json"
    )
}