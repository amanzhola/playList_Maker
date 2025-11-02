package com.example.playlistmaker.utils

import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.playlistmaker.R

// option from resources FailBlock1
@Composable
fun FailBlock1(@StringRes textId: Int, enabled: Boolean, topMargin: Dp = 50.dp) {
    AndroidView(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = topMargin), // внешний зазор сверху, если нужен
        factory = { ctx ->
            (LayoutInflater.from(ctx).inflate(R.layout.fail, null, false) as TextView).apply {
                // стиль SearchFail применится, НО мы поправим вертикальный сдвиг
                visibility = View.VISIBLE           // переопределяем GONE
                isEnabled = enabled                 // включает нужный item в fail_icon.xml
                setText(textId)

                // чтобы блок был сразу под инпутом
                setPadding(paddingLeft, 0, paddingRight, paddingBottom)
                // если используем RTL:
                // ViewCompat.setPaddingRelative(this, paddingStart, 0, paddingEnd, paddingBottom)

                // на всякий случай горизонтально по центру
                (layoutParams as? ViewGroup.MarginLayoutParams)?.let { lp ->
                    lp.width = ViewGroup.LayoutParams.WRAP_CONTENT
                    lp.height = ViewGroup.LayoutParams.WRAP_CONTENT
                }
                gravity = Gravity.CENTER_HORIZONTAL
            }
        },
        update = { tv ->
            tv.visibility = View.VISIBLE
            tv.isEnabled = enabled
            tv.setText(textId)
            // держим topPadding = 0 при обновлениях
            tv.setPadding(tv.paddingLeft, 0, tv.paddingRight, tv.paddingBottom)
        }
    )
}

// option local FailBlock
//@Composable
//fun FailBlock(@StringRes textId: Int, enabled: Boolean) {
//    val topPadding   = dimensionResource(R.dimen.track_45)
//    val drawablePad  = dimensionResource(R.dimen.Padding_16)
//    val textSizeSp   = dimensionResource(R.dimen.Search_Text_19).value.sp
//    val textColor    = colorResource(R.color.textColor_white)
//    val ysMedium     = remember { FontFamily(Font(R.font.ys_display_medium)) }
//
//    Column(
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(vertical = 24.dp),
//        horizontalAlignment = Alignment.CenterHorizontally
//    ) {
//        AndroidView(
//            // без .size(...) — позволяем wrap_content
//            modifier = Modifier.padding(top = topPadding),
//            factory = { ctx ->
//                ImageView(ctx).apply {
//                    setImageResource(R.drawable.fail_icon)
//                    isEnabled = enabled
//                    // критично:
//                    adjustViewBounds = true
//                    layoutParams = ViewGroup.LayoutParams(
//                        ViewGroup.LayoutParams.WRAP_CONTENT,
//                        ViewGroup.LayoutParams.WRAP_CONTENT
//                    )
//                    scaleType = ImageView.ScaleType.CENTER_INSIDE
//                }
//            },
//            update = { iv -> iv.isEnabled = enabled }
//        )
//
//        Spacer(Modifier.height(drawablePad))
//
//        Text(
//            text = stringResource(textId),
//            color = textColor,
//            fontSize = textSizeSp,
//            fontFamily = ysMedium,
//            fontWeight = FontWeight.W400,
//            textAlign = TextAlign.Center,
//            modifier = Modifier.fillMaxWidth(),
//            style = LocalTextStyle.current.copy(
//                platformStyle = PlatformTextStyle(includeFontPadding = true)
//            )
//        )
//    }
//}

enum class FailTextPlacement { Top, Bottom, Start, End }

@Composable
fun FailBlock(
    @StringRes textId: Int,
    enabled: Boolean,
    textPlacement: FailTextPlacement = FailTextPlacement.Bottom,
    topPadding: Dp? = null,  // null -> возьмём из R.dimen.track_45
) {
    val resolvedTopPad = topPadding ?: dimensionResource(R.dimen.track_45)
    val gapBetween = dimensionResource(R.dimen.Padding_16)
    val textSizeSp = dimensionResource(R.dimen.Search_Text_19).value.sp
    val textColor  = colorResource(R.color.textColor_white)
    val ysMedium   = remember { FontFamily(Font(R.font.ys_display_medium)) }

    // общий кусок с иконкой (AndroidView оставляем как у тебя)
    @Composable
    fun FailIcon() {
        AndroidView(
            modifier = Modifier,
            factory = { ctx ->
                ImageView(ctx).apply {
                    setImageResource(R.drawable.fail_icon)
                    isEnabled = enabled
                    adjustViewBounds = true
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                    scaleType = ImageView.ScaleType.CENTER_INSIDE
                }
            },
            update = { iv -> iv.isEnabled = enabled }
        )
    }

    @Composable
    fun FailText() {
        Text(
            text = stringResource(textId),
            color = textColor,
            fontSize = textSizeSp,
            fontFamily = ysMedium,
            fontWeight = FontWeight.W400,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
            style = androidx.compose.material3.LocalTextStyle.current.copy(
                platformStyle = PlatformTextStyle(includeFontPadding = true)
            )
        )
    }

    when (textPlacement) {
        FailTextPlacement.Top -> {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = resolvedTopPad, bottom = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                FailText()
                Spacer(Modifier.height(gapBetween))
                FailIcon()
            }
        }
        FailTextPlacement.Bottom -> {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = resolvedTopPad, bottom = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                FailIcon()
                Spacer(Modifier.height(gapBetween))
                FailText()
            }
        }
        FailTextPlacement.Start -> {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = resolvedTopPad, bottom = 24.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                FailText()
                Spacer(Modifier.width(gapBetween))
                FailIcon()
            }
        }
        FailTextPlacement.End -> {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = resolvedTopPad, bottom = 24.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                FailIcon()
                Spacer(Modifier.width(gapBetween))
                FailText()
            }
        }
    }
}
