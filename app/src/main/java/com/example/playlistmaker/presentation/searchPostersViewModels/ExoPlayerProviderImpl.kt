package com.example.playlistmaker.presentation.searchPostersViewModels

import android.content.Context
import androidx.media3.exoplayer.ExoPlayer

class ExoPlayerProviderImpl(
    private val appContext: Context
) : ExoPlayerProvider {
    override fun create(): ExoPlayer =
        ExoPlayer.Builder(appContext).build()
}