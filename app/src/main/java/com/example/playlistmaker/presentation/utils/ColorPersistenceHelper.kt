package com.example.playlistmaker.presentation.utils

import android.content.Context

class ColorPersistenceHelper(
    private val context: Context,
    private val isDarkTheme: Boolean
) {
    /** Новые методы: сохраняем по scope (экран/активити) */
    fun save(scope: String, segmentIndex: Int, color: Int) {
        ColorHelper.saveColor(context, scope, segmentIndex, isDarkTheme, color)
    }

    fun load(scope: String, segmentIndex: Int): Int? {
        return ColorHelper.loadColor(context, scope, segmentIndex, isDarkTheme)
    }

    fun clear(scope: String, range: IntRange) {
        ColorHelper.clearColors(context, scope, isDarkTheme, range)
    }

    // ⚠️ Адаптер для старого DI (принимал activityName)
    @Deprecated("DI: pass only isDarkTheme; scope передаётся в save/load/clear")
    constructor(context: Context, @Suppress("UNUSED_PARAMETER") activityName: String, isDarkTheme: Boolean)
            : this(context, isDarkTheme)

}
