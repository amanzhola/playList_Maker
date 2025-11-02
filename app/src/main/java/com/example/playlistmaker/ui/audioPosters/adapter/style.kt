package com.example.playlistmaker.ui.audioPosters.adapter

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.playlistmaker.R

private val YsDisplay = FontFamily(Font(R.font.ys_display_medium))

object LegacyTextStyles {
    @Composable fun text22_400() = TextStyle(
        fontSize = 22.sp,
        fontFamily = YsDisplay,
        fontWeight = FontWeight.W400,
        color = colorResource(R.color.textColor_white),
    )

    @Composable fun text14_400() = TextStyle(
        fontSize = 14.sp,
        fontFamily = YsDisplay,
        fontWeight = FontWeight.W400,
        color = colorResource(R.color.textColor_white),
    )

    // В XML у Text14 нет цвета — оставим без color, чтобы наследовал.
    val Text14 = TextStyle(
        fontSize = 14.sp,
        fontFamily = YsDisplay,
        fontWeight = FontWeight.W500,
    )

    @Composable fun text13_400() = TextStyle(
        fontSize = 13.sp,
        fontFamily = YsDisplay,
        fontWeight = FontWeight.W400,
        color = colorResource(R.color.switch_thumb_off_color),
    )

    // for create play list
    @Composable fun text16_400() = TextStyle(
        fontSize = 16.sp,
        fontFamily = FontFamily(Font(R.font.ys_display_regular)),
        fontWeight = FontWeight.W400,
        color = colorResource(R.color.textColor_white)
    )

    @Composable fun text16_500() = TextStyle(
        fontSize = 16.sp,
        fontFamily = FontFamily(Font(R.font.ys_display_regular)),
        fontWeight = FontWeight.W500,
        color = colorResource(R.color.white1) // для кнопки
    )

    // for playlist info
    @Composable fun text24_700() = TextStyle(
        fontSize = dimensionResource(R.dimen.Title_text_24).value.sp,
        fontFamily = FontFamily(Font(R.font.ys_display_bold)),
        fontWeight = FontWeight.W700,
        color = colorResource(R.color.textColor_white)
    )

    @Composable fun text18_400() = TextStyle(
        fontSize = dimensionResource(R.dimen.Search_Text_18).value.sp,
        fontFamily = YsDisplay,
        fontWeight = FontWeight.W400,
        color = colorResource(R.color.textColor_white)
    )

    @Composable fun text11_400() = TextStyle(
        fontSize = dimensionResource(R.dimen.Search_Text_11).value.sp,
        fontFamily = YsDisplay,
        fontWeight = FontWeight.W400
    )
}
