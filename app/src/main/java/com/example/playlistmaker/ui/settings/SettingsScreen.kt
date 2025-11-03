package com.example.playlistmaker.ui.settings

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.playlistmaker.R
import com.example.playlistmaker.presentation.utils.deriveFieldBgFromScreen
import com.google.android.material.switchmaterial.SwitchMaterial

/* ───────────────── helpers ───────────────── */

// option of own color alternative to deriveFieldBgFromScreen()
private fun Color.lighten(amount: Float): Color {
    // amount: 0f..1f — чем больше, тем светлее; просто смешиваем с белым
    val a = amount.coerceIn(0f, 1f)
    return Color(
        red   = this.red   + (1f - this.red)   * a,
        green = this.green + (1f - this.green) * a,
        blue  = this.blue  + (1f - this.blue)  * a,
        alpha = this.alpha
    )
}

/* ───────────────── screen ───────────────── */

@SuppressLint("LocalContextResourcesRead")
@Composable
fun SettingsScreen(
    darkMode: Boolean,
    onToggleDarkMode: (Boolean) -> Unit,
    onShare: () -> Unit,
    onSupport: () -> Unit,
    onAgreement: () -> Unit,

    // принимаем ТОЛЬКО 3 внешних цвета
    extBackground: Color? = null,   // фон
    extTextColor: Color? = null,    // текст
    extIconTint: Color? = null      // иконки + основа для Switch ON
) {
    // дефолты из ресурсов (как у вас сейчас)
    val screenBg = extBackground ?: colorResource(R.color.white_textColor)
    val ctx = LocalContext.current
    val density = LocalDensity.current
    val textColor = extTextColor ?: colorResource(R.color.black_white)
    val textSizeSp = with(density) { ctx.resources.getDimension(R.dimen.Settings_Text_16).toSp() }
    val ysRegular = FontFamily(Font(R.font.ys_display_regular, weight = FontWeight.W400))
    val text16 = TextStyle(
        fontSize = textSizeSp,
        fontFamily = ysRegular,
        fontWeight = FontWeight.W400,
        color = textColor
    )

    // цвет иконок: внешне заданный или hintColor по умолчанию
    val iconTint = extIconTint ?: colorResource(R.color.hintColor)

    // Switch:
    val switchThumbOn = colorResource(R.color.switch_thumb_on_color)
    val switchTrackOn = colorResource(R.color.switch_track_on_color)
    val switchThumbOff = iconTint

    val switchTrackOff =  deriveFieldBgFromScreen(iconTint)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(screenBg)
    ) {
        Spacer(Modifier.height(dimensionResource(R.dimen.Separator_10)))

        SwitchRowExact(
            title = stringResource(R.string.switch_r),
            checked = darkMode,
            rowEndPadding = dimensionResource(R.dimen.Switch_padding_6),
            gapTextToSwitch = dimensionResource(R.dimen.Switch_padding_6),
            textStyle = text16,
            rowBackground = screenBg,
            thumbOn = switchThumbOn,
            trackOn = switchTrackOn,
            thumbOff = switchThumbOff,
            trackOff = switchTrackOff,
            onCheckedChange = onToggleDarkMode
        )

        SettingsRowExact(
            title = stringResource(R.string.share_r),
            iconEndRes = R.drawable.share,
            textStyle = text16,
            rowBackground = screenBg,
            iconTint = iconTint,
            onClick = onShare
        )
        SettingsRowExact(
            title = stringResource(R.string.support_r),
            iconEndRes = R.drawable.group,
            textStyle = text16,
            rowBackground = screenBg,
            iconTint = iconTint,
            onClick = onSupport
        )
        SettingsRowExact(
            title = stringResource(R.string.agreement_r),
            iconEndRes = R.drawable.vector,
            textStyle = text16,
            rowBackground = screenBg,
            iconTint = iconTint,
            onClick = onAgreement
        )
    }
}

/* ───────────────── helpers (без изменений логики) ───────────────── */

@Composable
private fun MdcSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    thumbColor: Color,
    trackColor: Color
) {
    AndroidView(
        factory = { context ->
            SwitchMaterial(context).apply {
                thumbTintList = ColorStateList.valueOf(thumbColor.toArgb())
                trackTintList = ColorStateList.valueOf(trackColor.toArgb())
                isChecked = checked
                setOnCheckedChangeListener { _, isC -> onCheckedChange(isC) }
            }
        },
        update = { view ->
            if (view.isChecked != checked) view.isChecked = checked
            view.thumbTintList = ColorStateList.valueOf(thumbColor.toArgb())
            view.trackTintList = ColorStateList.valueOf(trackColor.toArgb())
        }
    )
}

@Composable
private fun SwitchRowExact(
    title: String,
    checked: Boolean,
    rowEndPadding: Dp,
    gapTextToSwitch: Dp,
    textStyle: TextStyle,
    rowBackground: Color,
    thumbOn: Color,
    trackOn: Color,
    thumbOff: Color,
    trackOff: Color,
    onCheckedChange: (Boolean) -> Unit
) {
    val rowHeight = dimensionResource(R.dimen.Settings_Height_61)
    val padStart = dimensionResource(R.dimen.Padding_16)

    Surface(
        tonalElevation = 0.dp,
        color = rowBackground,
        shape = RoundedCornerShape(0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(rowHeight)
                .padding(start = padStart, end = rowEndPadding),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = textStyle,
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.width(gapTextToSwitch))
            MdcSwitch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                thumbColor = if (checked) thumbOn else thumbOff,
                trackColor = if (checked) trackOn else trackOff
            )
        }
    }
}

@Composable
private fun SettingsRowExact(
    title: String,
    iconEndRes: Int,
    textStyle: TextStyle,
    rowBackground: Color,
    iconTint: Color,
    onClick: () -> Unit
) {
    val rowHeight = dimensionResource(R.dimen.Settings_Height_61)
    val padStart = dimensionResource(R.dimen.Padding_16)
    val padEnd = dimensionResource(R.dimen.Padding_18)

    Surface(
        tonalElevation = 0.dp,
        color = rowBackground,
        shape = RoundedCornerShape(0.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(rowHeight)
                .padding(start = padStart, end = padEnd),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = textStyle,
                modifier = Modifier.weight(1f)
            )
            Icon(
                painter = painterResource(iconEndRes),
                contentDescription = null,
                tint = iconTint
            )
        }
    }
}
