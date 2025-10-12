package com.example.playlistmaker.presentation.utils

import android.content.Context
import com.example.playlistmaker.BaseActivity
import com.example.playlistmaker.R
import com.example.playlistmaker.ui.movie.SearchMovie
import com.example.playlistmaker.ui.weather.SearchWeather

object SegmentTextHelper {

    private fun resolveScreenType(context: Context): ScreenType {
        val activity = context as? BaseActivity ?: return when (context) {
            is SearchMovie -> ScreenType.SEARCH_MOVIE_ACTIVITY
            is SearchWeather -> ScreenType.SEARCH_WEATHER_ACTIVITY
            else -> ScreenType.UNKNOWN
        }
        return activity.getCurrentFragment().toScreenType()
    }

    fun getSegmentTexts(context: Context, isMain: Boolean): Array<String> {
        val screen = resolveScreenType(context)
        val fifthText = when {
            isMain -> context.getString(R.string.toDefault)
            screen == ScreenType.AUDIO_FRAGMENT -> context.getString(R.string.youtube)   // ← YouTube
            else -> context.getString(R.string.navigation)
        }

        return arrayOf(
            context.getString(R.string.switch_short),
            context.getString(R.string.share_short), // 🎶
            context.getString(R.string.support_short),
            context.getString(R.string.agreement_short),
            fifthText,
            context.getString(R.string.language)
        )
    }

    fun getNewSegmentTexts(context: Context, isMain: Boolean): Array<String> {
        return arrayOf(
            context.getString(R.string.set_titleColor), // "Цвет заглавия",
            context.getString(R.string.set_backgroundColor),// "Цвет фона",
            context.getString(R.string.set_btnTextColor), // "Цвет текста текста",
            context.getString(R.string.set_iconColor), // "Цвет иконок",
            if (isMain) context.getString(R.string.set_btnBackgroundColor) else context.getString(R.string.language),// "Цвет фона кнопки",
            if (isMain) context.getString(R.string.language) else context.getString(R.string.toDefault) // "По умолчанию" 🧹
        )
    }

    fun getSegmentIcons(context: Context): IntArray {
        val screen = resolveScreenType(context)

        val secondIcon = when (screen) {
            ScreenType.SEARCH_FRAGMENT -> R.drawable.queue_music_24
            ScreenType.AUDIO_FRAGMENT -> R.drawable.music_note_24
            ScreenType.SEARCH_MOVIE_ACTIVITY -> R.drawable.move_down_24
            else -> R.drawable.share
        }

        val fifthIcon = when (screen) {
            ScreenType.MAIN_FRAGMENT  -> R.drawable.color_24
            ScreenType.AUDIO_FRAGMENT -> R.drawable.ic_youtube_24    // ← новая иконка
            else -> R.drawable.navigation_24
        }

        return intArrayOf(
            R.drawable.switch_24,
            secondIcon,
            R.drawable.group,
            R.drawable.vector,
            fifthIcon,
            R.drawable.translate_24
        )
    }

    fun getNewSegmentIcons(context: Context): IntArray {
        val screen = resolveScreenType(context)
        val isMain = screen == ScreenType.MAIN_FRAGMENT

        val fifthIcon = if (isMain) R.drawable.background_24 else R.drawable.translate_24
        val sixthIcon = if (isMain) R.drawable.translate_24 else R.drawable.color_24

        return intArrayOf( // 2️⃣ 👉 💾
            R.drawable.text_color_24,
            R.drawable.background_24,
            R.drawable.text_color_24,
            R.drawable.color_24,
            fifthIcon,
            sixthIcon
        )
    }
}
