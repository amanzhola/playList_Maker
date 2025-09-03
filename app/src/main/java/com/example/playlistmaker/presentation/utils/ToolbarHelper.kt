package com.example.playlistmaker.presentation.utils

import android.app.Activity
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.example.playlistmaker.R

data class ToolbarConfig( // 📍 👏
    val backArrowVisibility: Int,
    val titleResId: Int,
    val titleClickListener: (() -> Unit)? = null,
)

class ToolbarHelper(private val activity: Activity) {

    private var toolbar: Toolbar? = null
    private var title: TextView? = null
    private var backArrow: ImageView? = null

    fun initialize(config: ToolbarConfig, isMainActivity: Boolean) {
        toolbar = activity.findViewById(R.id.toolbar) ?: return
        title = toolbar!!.findViewById(R.id.title)
        backArrow = toolbar!!.findViewById(R.id.backArrow)

        val appCompatActivity = activity as? AppCompatActivity
        appCompatActivity?.setSupportActionBar(toolbar)
        appCompatActivity?.supportActionBar?.setDisplayShowTitleEnabled(false)

        title?.isEnabled = isMainActivity || config.backArrowVisibility != View.VISIBLE

        updateToolbar(config)
    }

    fun updateToolbar(config: ToolbarConfig) {
        backArrow?.visibility = config.backArrowVisibility
        title?.setText(config.titleResId)

        backArrow?.setOnClickListener(null)
        title?.setOnClickListener(null)

        if (config.backArrowVisibility == View.VISIBLE) {
            backArrow?.setOnClickListener { config.titleClickListener?.invoke() }
        } else {
            title?.setOnClickListener { config.titleClickListener?.invoke() }
        }
    }

    fun setTitleTextColor(color: Int) {
        title?.setTextColor(color)
    }

    fun setBackArrowColor(color: Int) {
        backArrow?.imageTintList = android.content.res.ColorStateList.valueOf(color)
    }
    // add background color
    fun setToolbarBackgroundColor(color: Int) {
        toolbar?.setBackgroundColor(color)
    }

    // fixing theme on emulator and real mobile difference
    /** Применить цвета из ТЕКУЩЕЙ темы (Day/Night) */
    fun applyThemeColors() {
        val tb = toolbar ?: return
        // Берём не «жёсткие» R.color.*, а атрибуты темы
        val bg = com.google.android.material.color.MaterialColors.getColor(
            tb, R.attr.toolbarColor
        )
        val on = com.google.android.material.color.MaterialColors.getColor(
            tb, R.attr.toolbarContentColor
        )

        tb.setBackgroundColor(bg)
        title?.setTextColor(on)
        // стрелка назад и overflow-меню
        backArrow?.imageTintList = android.content.res.ColorStateList.valueOf(on)
        (activity as? AppCompatActivity)?.let {
            tb.navigationIcon?.setTint(on)
            tb.overflowIcon?.setTint(on)
        }
    }

    fun applyMainBlueColors() {
        val tb = toolbar ?: return
        // Синий фон + белые иконки/текст (цвета из ресурсов — можно сделать day/night-aware при желании)
        val bg = tb.context.getColor(R.color.blue_textColor)      // твой «синий»
        val on = tb.context.getColor(R.color.white_white)    // белый
        tb.setBackgroundColor(bg)
        title?.setTextColor(on)
        backArrow?.imageTintList = android.content.res.ColorStateList.valueOf(on)
        (activity as? AppCompatActivity)?.let {
            tb.navigationIcon?.setTint(on)
            tb.overflowIcon?.setTint(on)
        }
    }
}
