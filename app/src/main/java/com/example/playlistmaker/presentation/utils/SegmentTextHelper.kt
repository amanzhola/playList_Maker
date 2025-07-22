package com.example.playlistmaker.presentation.utils

import android.content.Context
import com.example.playlistmaker.BaseActivity
import com.example.playlistmaker.R
import com.example.playlistmaker.ui.audio.SearchFragment
import com.example.playlistmaker.ui.audioPosters.ExtraOption
import com.example.playlistmaker.ui.main.MainFragment
import com.example.playlistmaker.ui.movie.SearchMovie

object SegmentTextHelper {

    fun getSegmentTexts(context: Context, isMain: Boolean): Array<String> {

        val currentActivity = context as? BaseActivity
        val currentFragment = currentActivity?.getCurrentFragment()
        val isMainFragment = currentFragment is MainFragment

        return arrayOf(
            context.getString(R.string.switch_short),
            context.getString(R.string.share_short), // 🎶
            context.getString(R.string.support_short),
            context.getString(R.string.agreement_short),
            if (isMainFragment) context.getString(R.string.toDefault) else context.getString(R.string.navigation),
            context.getString(R.string.language)
        )
    }

    fun getNewSegmentTexts(context: Context, isMain: Boolean): Array<String> {

        val currentActivity = context as? BaseActivity
        val currentFragment = currentActivity?.getCurrentFragment()
        val isMainFragment = currentFragment is MainFragment

        return arrayOf(
            context.getString(R.string.set_titleColor), // "Цвет заглавия",
            context.getString(R.string.set_backgroundColor), // "Цвет фона",
            context.getString(R.string.set_btnTextColor), // "Цвет текста текста",
            context.getString(R.string.set_iconColor), // "Цвет иконок",
            if (isMainFragment) context.getString(R.string.set_btnBackgroundColor) else context.getString(R.string.language), // "Цвет фона кнопки",
            if (isMainFragment) context.getString(R.string.language) else context.getString(R.string.toDefault) // "По умолчанию" 🧹
        )
    }

    fun getSegmentIcons(context: Context): IntArray {
        val currentActivity = context as? BaseActivity
        val currentFragment = currentActivity?.getCurrentFragment()
        val isMainFragment = currentFragment is MainFragment

        return intArrayOf(
            R.drawable.switch_24,

            when {
                currentFragment is SearchFragment -> R.drawable.queue_music_24
                context is ExtraOption -> R.drawable.music_note_24
                context is SearchMovie -> R.drawable.move_down_24
                else -> R.drawable.share
            },

            R.drawable.group,
            R.drawable.vector,
            if (isMainFragment) R.drawable.color_24 else R.drawable.navigation_24,
            R.drawable.translate_24
        )
    }

    fun getNewSegmentIcons(context: Context): IntArray {

        val currentActivity = context as? BaseActivity
        val currentFragment = currentActivity?.getCurrentFragment()
        val isMainFragment = currentFragment is MainFragment

        return intArrayOf( // 2️⃣ 👉 💾
            R.drawable.text_color_24,
            R.drawable.background_24,
            R.drawable.text_color_24,
            R.drawable.color_24,
            if (isMainFragment) R.drawable.background_24 else R.drawable.translate_24,
            if (isMainFragment) R.drawable.translate_24 else R.drawable.color_24
        )
    }
}
