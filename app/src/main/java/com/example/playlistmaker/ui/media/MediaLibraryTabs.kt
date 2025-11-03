package com.example.playlistmaker.ui.media

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import com.example.playlistmaker.R
import kotlinx.coroutines.launch
/* ======================= ИНТЕРФЕЙС ДЛЯ ХЕЛПЕРА ======================= */

interface MediaActiveTabProvider {
    /** 0 = Favourite, 1 = Playlists */
    fun currentMediaTab(): Int
}

@Composable
fun MediaTabsBarPure(
    selectedIndex: Int,
    titlesRes: List<Int>,
    onTabClick: (Int) -> Unit
) {
    val scope = rememberCoroutineScope()

    val bgColor      = colorResource(R.color.white_textColor)
    val textSelected = colorResource(R.color.textColor_white)
    val indicatorH   = dimensionResource(R.dimen.radius_2dp)

    SecondaryTabRow(
        selectedTabIndex = selectedIndex,
        containerColor = bgColor,
        divider = {}, // убираем серую линию
        indicator = {
            TabRowDefaults.SecondaryIndicator(
                modifier = Modifier
                    .tabIndicatorOffset(
                        selectedTabIndex = selectedIndex,
                        matchContentSize = false // индикатор = ширина вкладки
                    )
                    .height(indicatorH),
                color = textSelected
            )
        },
        modifier = Modifier.padding(
            start = dimensionResource(R.dimen.Padding_16),
            end   = dimensionResource(R.dimen.Padding_16)
        )
    ) {
        titlesRes.forEachIndexed { index, titleRes ->
            val selected = selectedIndex == index
            Tab(
                selected = selected,
                onClick = { scope.launch { onTabClick(index) } },
                text = {
                    Text(
                        text = stringResource(titleRes),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontSize = 14.sp,
                        fontFamily = FontFamily(Font(R.font.ys_display_medium)),
                        fontWeight = FontWeight.Medium,
                        color = colorResource(R.color.textColor_white)
                    )
                }
            )
        }
    }
}

