package com.example.playlistmaker.presentation.utils

import android.app.Activity
import android.util.TypedValue
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.example.playlistmaker.R
import com.google.android.material.color.MaterialColors

data class ToolbarConfig( // 📍 👏
    val backArrowVisibility: Int,
    val titleResId: Int,
    val titleClickListener: (() -> Unit)? = null,
)

class ToolbarHelper(private val activity: Activity) {

    private fun Int.hex(): String = String.format("#%08X", this)

    private var toolbar: Toolbar? = null
    private var title: TextView? = null
    private var backArrow: ImageView? = null

    private fun resolveColorOrNull(view: View, @AttrRes attr: Int): Int? {
        val tv = TypedValue()
        return if (view.context.theme.resolveAttribute(attr, tv, true)) tv.data else null
    }

    fun initialize(config: ToolbarConfig, isMainActivity: Boolean) {
        toolbar = activity.findViewById(R.id.toolbar) ?: run {
            android.util.Log.w("TITLEFLOW","ToolbarHelper.initialize: toolbar==null")
            return
        }
        title = toolbar!!.findViewById(R.id.title)
        backArrow = toolbar!!.findViewById(R.id.backArrow)

        (activity as? AppCompatActivity)?.apply {
            setSupportActionBar(toolbar)
            supportActionBar?.setDisplayShowTitleEnabled(false)
        }

        android.util.Log.d("TITLEFLOW",
            "initialize -> isMain=$isMainActivity, back=${config.backArrowVisibility}, res=${config.titleResId}"
        )
        updateToolbar(config)
    }

    fun updateToolbar(config: ToolbarConfig) {
        android.util.Log.d("TITLEFLOW",
            "updateToolbar enter: back=${config.backArrowVisibility}, res=${config.titleResId}, " +
                    "curTitle='${title?.text}'"
        )
        backArrow?.visibility = config.backArrowVisibility

        if (config.titleResId != 0) {
            title?.setText(config.titleResId)
            android.util.Log.d("TITLEFLOW",
                "updateToolbar setTitle(fromRes=${config.titleResId}) -> now='${title?.text}'"
            )
        } else {
            android.util.Log.d("TITLEFLOW","updateToolbar skip title (resId=0)")
        }

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
        android.util.Log.d("TITLEFLOW","setTitleTextColor -> ${color.hex()}")
    }

    fun setBackArrowColor(color: Int) {
        backArrow?.imageTintList = android.content.res.ColorStateList.valueOf(color)
        android.util.Log.d("TITLEFLOW","setBackArrowColor -> ${color.hex()}")
    }

    fun setToolbarBackgroundColor(color: Int) {
        toolbar?.setBackgroundColor(color)
        android.util.Log.d("TITLEFLOW","setToolbarBackgroundColor -> ${color.hex()}")
    }

    fun applyThemeColors() {
        val tb = toolbar ?: run {
            android.util.Log.w("TITLEFLOW","applyThemeColors: toolbar==null")
            return
        }
        val bg = resolveColorOrNull(tb, R.attr.toolbarColor)
            ?: MaterialColors.getColor(tb, com.google.android.material.R.attr.colorSurface)
        val on = resolveColorOrNull(tb, R.attr.toolbarContentColor)
            ?: MaterialColors.getColor(tb, com.google.android.material.R.attr.colorOnSurface)

        tb.setBackgroundColor(bg)
        title?.setTextColor(on)
        backArrow?.imageTintList = android.content.res.ColorStateList.valueOf(on)
        (activity as? AppCompatActivity)?.let {
            tb.navigationIcon?.setTint(on)
            tb.overflowIcon?.setTint(on)
        }
        android.util.Log.d("TITLEFLOW",
            "applyThemeColors -> bg=${bg.hex()} on=${on.hex()} titleNow='${title?.text}'"
        )
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
    //************************************************************
    // fixing toolbar apply and saving background color

    fun setBackgroundColor(@ColorInt color: Int) {
        toolbar?.setBackgroundColor(color)
    }

    fun setTitle(text: CharSequence) {
        title?.text = text
        android.util.Log.d("TITLEFLOW","setTitle(text='$text')")
    }

    fun dumpState(tag: String = "TITLEFLOW") {
        val tb = toolbar
        val t  = title
        val txt = t?.text
        val color = t?.currentTextColor
        android.util.Log.d(tag, "dumpState: title='$txt', titleColor=${color?.hex()}, tb=${tb}")
    }
}
