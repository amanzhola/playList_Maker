package com.example.playlistmaker.utils

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp
import com.example.playlistmaker.R

@Composable
fun UpdateButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    minHeight: Dp = dimensionResource(R.dimen.searchLineHeight_52),
) {
    val corner    = dimensionResource(R.dimen.track_45)
    val textColor = colorResource(R.color.white_textColor)
    val container = colorResource(R.color.textColor_white)

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {
        Button(
            onClick = onClick,
            shape = RoundedCornerShape(corner),
            colors = ButtonDefaults.buttonColors(containerColor = container),
            modifier = Modifier.defaultMinSize(minHeight = minHeight)
        ) {
            Text(
                text = text,
                color = textColor,
                fontSize = 14.sp, // Text14
                fontFamily = FontFamily(Font(R.font.ys_display_medium)),
                fontWeight = FontWeight.Medium
            )
        }
    }
}

/** Удобная перегрузка для ресурсов */
@Composable
fun UpdateButton(
    @StringRes textRes: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    minHeight: Dp = dimensionResource(R.dimen.searchLineHeight_52),
) = UpdateButton(
    text = stringResource(textRes),
    onClick = onClick,
    modifier = modifier,
    minHeight = minHeight
)
