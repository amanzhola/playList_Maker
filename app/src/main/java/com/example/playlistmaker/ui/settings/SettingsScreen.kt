package com.example.playlistmaker.ui.settings

import android.annotation.SuppressLint
import android.content.res.ColorStateList
<<<<<<< Updated upstream
=======
import android.view.LayoutInflater
>>>>>>> Stashed changes
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
<<<<<<< Updated upstream
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

=======
import com.google.android.material.switchmaterial.SwitchMaterial

>>>>>>> Stashed changes
@SuppressLint("LocalContextResourcesRead")
@Composable
fun SettingsScreen(
    darkMode: Boolean,
    onToggleDarkMode: (Boolean) -> Unit,
    onShare: () -> Unit,
    onSupport: () -> Unit,
    onAgreement: () -> Unit,
<<<<<<< Updated upstream

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
=======
) {
    // фон экрана из XML (day/night)
    val screenBg = colorResource(R.color.white_textColor)

    // текстовый стиль Text16 из XML
    val ctx = LocalContext.current
    val density = LocalDensity.current
    val textColor = colorResource(R.color.black_white)
>>>>>>> Stashed changes
    val textSizeSp = with(density) { ctx.resources.getDimension(R.dimen.Settings_Text_16).toSp() }
    val ysRegular = FontFamily(Font(R.font.ys_display_regular, weight = FontWeight.W400))
    val text16 = TextStyle(
        fontSize = textSizeSp,
        fontFamily = ysRegular,
        fontWeight = FontWeight.W400,
        color = textColor
    )

<<<<<<< Updated upstream
    // цвет иконок: внешне заданный или hintColor по умолчанию
    val iconTint = extIconTint ?: colorResource(R.color.hintColor)

    // Switch:
    val switchThumbOn = colorResource(R.color.switch_thumb_on_color)
    val switchTrackOn = colorResource(R.color.switch_track_on_color)
    val switchThumbOff = iconTint

    val switchTrackOff =  deriveFieldBgFromScreen(iconTint)

=======
>>>>>>> Stashed changes
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(screenBg)
    ) {
<<<<<<< Updated upstream
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

=======
        // include @layout/fail
        IncludeFailXml()

        // marginTop из SwitchView
        Spacer(Modifier.height(dimensionResource(R.dimen.Separator_10)))

        // строка со свитчем (правый край как у строк с иконками + зазор между текстом и свитчем)
        SwitchRowExact(
            title = stringResource(R.string.switch_r),
            checked = darkMode,
            rowEndPadding = dimensionResource(R.dimen.Switch_padding_6),       // выравнивание по правому краю
            gapTextToSwitch = dimensionResource(R.dimen.Switch_padding_6), // как android:paddingEnd у Switch
            textStyle = text16,
            rowBackground = screenBg,
            // цвета свитча (day/night) — как в стилях SwitchView
            thumbOn = colorResource(R.color.switch_thumb_on_color),
            trackOn = colorResource(R.color.switch_track_on_color),
            thumbOff = colorResource(R.color.switch_thumb_off_color),
            trackOff = colorResource(R.color.switch_track_off_color),
            onCheckedChange = onToggleDarkMode
        )

        // три строки как SettingsTextView
>>>>>>> Stashed changes
        SettingsRowExact(
            title = stringResource(R.string.share_r),
            iconEndRes = R.drawable.share,
            textStyle = text16,
            rowBackground = screenBg,
<<<<<<< Updated upstream
            iconTint = iconTint,
=======
>>>>>>> Stashed changes
            onClick = onShare
        )
        SettingsRowExact(
            title = stringResource(R.string.support_r),
            iconEndRes = R.drawable.group,
            textStyle = text16,
            rowBackground = screenBg,
<<<<<<< Updated upstream
            iconTint = iconTint,
=======
>>>>>>> Stashed changes
            onClick = onSupport
        )
        SettingsRowExact(
            title = stringResource(R.string.agreement_r),
            iconEndRes = R.drawable.vector,
            textStyle = text16,
            rowBackground = screenBg,
<<<<<<< Updated upstream
            iconTint = iconTint,
=======
>>>>>>> Stashed changes
            onClick = onAgreement
        )
    }
}

<<<<<<< Updated upstream
/* ───────────────── helpers (без изменений логики) ───────────────── */

@Composable
=======
/* ===== helpers ===== */

@Composable
private fun IncludeFailXml() {
    AndroidView(
        modifier = Modifier.fillMaxWidth(),
        factory = { ctx ->
            LayoutInflater.from(ctx).inflate(R.layout.fail, null, false)
        }
    )
}

// <<<<<<<<<<<<< ВАЖНО: SwitchMaterial во View через AndroidView >>>>>>>>>>
@Composable
>>>>>>> Stashed changes
private fun MdcSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    thumbColor: Color,
    trackColor: Color
) {
    AndroidView(
        factory = { context ->
            SwitchMaterial(context).apply {
<<<<<<< Updated upstream
=======
                // размеры/фон/состояния возьмутся из темы MDC; выставим цвета как в стилях
>>>>>>> Stashed changes
                thumbTintList = ColorStateList.valueOf(thumbColor.toArgb())
                trackTintList = ColorStateList.valueOf(trackColor.toArgb())
                isChecked = checked
                setOnCheckedChangeListener { _, isC -> onCheckedChange(isC) }
            }
        },
        update = { view ->
            if (view.isChecked != checked) view.isChecked = checked
<<<<<<< Updated upstream
=======
            // tint'ы держим синхронно, если сменили тему
>>>>>>> Stashed changes
            view.thumbTintList = ColorStateList.valueOf(thumbColor.toArgb())
            view.trackTintList = ColorStateList.valueOf(trackColor.toArgb())
        }
    )
}

<<<<<<< Updated upstream
=======
/**
 * Строка со свитчем:
 * - правый отступ = Padding_18 (как у строк с иконками) → выравнивание по правому краю
 * - зазор между текстом и свитчем = Switch_padding_6 (как android:paddingEnd у SwitchMaterial)
 * - внутри строки фон = white_textColor
 * - сам свитч = SwitchMaterial (MDC), вид 1-в-1 как во View
 */
>>>>>>> Stashed changes
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
<<<<<<< Updated upstream
=======
            // используем MDC Switch, чтобы внешний вид был как у SwitchMaterial из XML
>>>>>>> Stashed changes
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
<<<<<<< Updated upstream
    iconTint: Color,
=======
>>>>>>> Stashed changes
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
<<<<<<< Updated upstream
                tint = iconTint
=======
                tint = Color.Unspecified
>>>>>>> Stashed changes
            )
        }
    }
}
