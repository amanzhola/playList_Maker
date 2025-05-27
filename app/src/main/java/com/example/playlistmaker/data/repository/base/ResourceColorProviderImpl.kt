package com.example.playlistmaker.data.repository.base

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Resources
import android.os.Build
import com.example.playlistmaker.domain.repository.base.ResourceColorProvider

class ResourceColorProviderImpl(private val context: Context) : ResourceColorProvider {
    @SuppressLint("ObsoleteSdkInt")
    override fun getColor(id: Int, theme: Resources.Theme?): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            context.resources.getColor(id, theme ?: context.theme)
        } else {
            @Suppress("DEPRECATION")
            context.resources.getColor(id)
        }
    }
}