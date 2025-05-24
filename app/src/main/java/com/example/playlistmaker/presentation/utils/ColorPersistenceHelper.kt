package com.example.playlistmaker.presentation.utils

import android.content.Context

class ColorPersistenceHelper(
    private val context: Context,
    private val activityName: String,
    private val isDarkTheme: Boolean
) {
    fun save(segmentIndex: Int, color: Int) {
        ColorHelper.saveColor(context, activityName, segmentIndex, isDarkTheme, color)
    }

    fun load(segmentIndex: Int): Int? {
        return ColorHelper.loadColor(context, activityName, segmentIndex, isDarkTheme)
    }

    fun clear(range: IntRange) {
        ColorHelper.clearColors(context, activityName, isDarkTheme, range)
    }
}
