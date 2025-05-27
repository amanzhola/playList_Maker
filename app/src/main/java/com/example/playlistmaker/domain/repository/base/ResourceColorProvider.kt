package com.example.playlistmaker.domain.repository.base

import android.content.res.Resources

interface ResourceColorProvider {
    fun getColor(id: Int, theme: Resources.Theme? = null): Int
}