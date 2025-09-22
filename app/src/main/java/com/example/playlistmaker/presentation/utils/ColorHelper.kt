package com.example.playlistmaker.presentation.utils

import android.annotation.SuppressLint
import android.content.Context
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.content.ContextCompat
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
    //**********************************************************************

    private fun key(scope: String, slot: Int, dark: Boolean) = // 📤  👍
        "${scope}_segment_${slot}_${if (dark) "dark" else "light"}" // 🌓

    @SuppressLint("UseKtx")
    fun saveColor(ctx: Context, scope: String, slot: Int, dark: Boolean, color: Int) {
        ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit().putInt(key(scope, slot, dark), color).apply()
    }

    fun loadColor(ctx: Context, scope: String, slot: Int, dark: Boolean): Int? {
        val v = ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getInt(key(scope, slot, dark), -1)
        return if (v == -1) null else v
    } //  😉 💡 👉 🔄 👈

    @SuppressLint("UseKtx")
    fun clearColors(ctx: Context, scope: String, dark: Boolean, range: IntRange) {
        val e = ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit()
        range.forEach { e.remove(key(scope, it, dark)) } // 🧠
        e.apply()
    }
}
