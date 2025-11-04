@file:OptIn(androidx.media3.common.util.UnstableApi::class)
package com.example.playlistmaker.presentation.searchPostersViewModels

import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.cache.Cache
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory

class ExoPlayerProviderImpl(
    private val appContext: Context,
    private val cache: Cache,
) : ExoPlayerProvider {

@OptIn(UnstableApi::class)
override fun create(): ExoPlayer {
        // 1) «Больше буферов» — меньше ребуферов
        val loadControl = DefaultLoadControl.Builder()
            .setBufferDurationsMs(
                /* minBufferMs        = */ 25_000,
                /* maxBufferMs        = */ 50_000,
                /* bufferForPlaybackMs = */ 1_500,
                /* bufferForPlaybackAfterRebufferMs = */ 3_000
            )
            .build()

        // 2) Источник данных: либо через кэш, либо напрямую
        val upstream: DataSource.Factory = DefaultDataSource.Factory(appContext)

        val dataSourceFactory: androidx.media3.datasource.DataSource.Factory =
        androidx.media3.datasource.cache.CacheDataSource.Factory()
            .setCache(cache)
            .setUpstreamDataSourceFactory(upstream)
            .setFlags(androidx.media3.datasource.cache.CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)

        val mediaSourceFactory = DefaultMediaSourceFactory(dataSourceFactory)

        // 3) Собираем плеер
        return ExoPlayer.Builder(appContext)
            .setLoadControl(loadControl)
            .setMediaSourceFactory(mediaSourceFactory)
            .build()
    }
}