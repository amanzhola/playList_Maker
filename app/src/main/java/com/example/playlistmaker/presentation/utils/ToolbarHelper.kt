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
    val titleClickListener: (() -> Unit)? = null
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
}
