package com.example.playlistmaker.presentation.utils

import android.content.Context
import android.content.SharedPreferences
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.view.children
import com.example.playlistmaker.ColorProvider
import com.google.android.material.button.MaterialButton

object ColorHelper {

    private const val PREF_NAME = "color_preferences"
    private var currentIndex = 0

    fun getNextColor(context: Context): Int {
        val colorResId = ColorProvider.colors[currentIndex]
        val color = ContextCompat.getColor(context, colorResId)
        currentIndex = (currentIndex + 1) % ColorProvider.colors.size
        return color
    }

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun saveColor(context: Context, activityName: String, segmentIndex: Int, isDarkTheme: Boolean, color: Int) {
        val key = generateKey(activityName, segmentIndex, isDarkTheme)
        getPrefs(context).edit() { putInt(key, color) }
    }

    fun loadColor(context: Context, activityName: String, segmentIndex: Int, isDarkTheme: Boolean): Int? {
        val key = generateKey(activityName, segmentIndex, isDarkTheme)
        val color = getPrefs(context).getInt(key, -1)
        return if (color != -1) color else null
    } //  😉 💡 👉 🔄 👈

    fun clearColors(context: Context, activityName: String, isDarkTheme: Boolean, segmentRange: IntRange) {
        getPrefs(context).edit() {
            segmentRange.forEach {
                remove(generateKey(activityName, it, isDarkTheme)) // 🧠
            }
        }
    }

    private fun generateKey(activityName: String, segmentIndex: Int, isDarkTheme: Boolean): String { // 📤  👍
        val themeSuffix = if (isDarkTheme) "_dark" else "_light" // 🌓
        return "${activityName}_segment_$segmentIndex$themeSuffix"
    }

    fun ViewGroup.changeTextColor(color: Int, ignoreId: Int? = null) { // 🎨
        children.forEach { view ->
            if (view.id == ignoreId) return@forEach // 🔵 ❓ ❗
            when (view) {
                is TextView -> view.setTextColor(color)
                is ViewGroup -> view.changeTextColor(color, ignoreId) //  🔝 ✍️
            }
        }
    }

    fun ViewGroup.changeIconColor(color: Int, buttonIds: List<Int>) {
        for (id in buttonIds) {
            val button = findViewById<MaterialButton?>(id) // 👨‍🔧
            button?.icon?.let { icon -> // 👇
                DrawableCompat.setTint(icon, color)
                button.icon = icon
            }
        }
    } // 🧠

    fun ViewGroup.changeBackgroundColor(color: Int) {
        children.forEach { view -> // 📚
            when (view) {
                is Button -> view.setBackgroundColor(color)
                is ViewGroup -> view.changeBackgroundColor(color) //  🤘
            }
        }
    } //  👌

    fun ViewGroup.changeCompoundDrawableColor(color: Int, ignoreId: Int? = null) { // ❌ 🚀
        children.forEach { view -> // 📚
            if (view.id == ignoreId) return@forEach // ➡️
            when (view) {
                is TextView -> {
                    val drawable = view.compoundDrawablesRelative[2]
                    drawable?.setTint(color)
                    view.setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, drawable, null)
                }
                is ViewGroup -> view.changeCompoundDrawableColor(color, ignoreId) //  🤘
            }
        }
    }
}
