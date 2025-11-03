package com.example.playlistmaker.ui.audioPosters.sheet

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import com.example.playlistmaker.R
import com.example.playlistmaker.domain.models.playlist.Playlist
import com.example.playlistmaker.presentation.ImageLoader
import com.example.playlistmaker.utils.UpdateButton

@Composable
fun PlaylistsSheet(
    playlists: List<Playlist>,
    imageLoader: ImageLoader,
    onPlaylistClick: (Playlist) -> Unit,
    onCreatePlaylistClick: () -> Unit
) {
    val titleColor = colorResource(R.color.textColor_white)
    val hintColor = colorResource(R.color.hintColor_white)

    // размеры/отступы из dimen'ов
    val titleTop = dimensionResource(R.dimen.arrow_back_padding_12)
    val titlePadding = dimensionResource(R.dimen.Padding_16)
    val listPaddingBottom = dimensionResource(R.dimen.Padding_16)

    // 3) Заголовок — как в XML: стиль Text19 + цвет + центрирование
    val text19Dp = dimensionResource(R.dimen.Search_Text_19)
    val text19Sp = with(LocalDensity.current) { text19Dp.value.sp } // аккуратно конвертируем dimen в sp

    Text(
        text = stringResource(R.string.add_to_playlist),
        color = titleColor,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = titleTop)
            .padding(horizontal = titlePadding),
        textAlign = TextAlign.Center, // ← центр
        // стиль Text19: font=ys_display_medium, weight=400, size=@dimen/Search_Text_19
        style = MaterialTheme.typography.bodyLarge.copy(
            fontSize = text19Sp,
            fontFamily = androidx.compose.ui.text.font.FontFamily(
                androidx.compose.ui.text.font.Font(R.font.ys_display_medium)
            ),
            fontWeight = androidx.compose.ui.text.font.FontWeight.W400
        )
    )

    // 4) Кнопка — переиспользуем UpdateButton, только с текстом @string/newPlaylist
    UpdateButton(
        textRes = R.string.newPlaylist,
        onClick = onCreatePlaylistClick,
        modifier = Modifier
            .padding(horizontal = titlePadding, vertical = dimensionResource(R.dimen.Padding_16))
    )

    // Список плейлистов (как item_playlist_bottom.xml по цветам/отступам/высотам)
    LazyColumn(
        contentPadding = PaddingValues(
            bottom = listPaddingBottom
        ),
        verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.Drawable_padding_8))
    ) {
        items(
            items = playlists,
            key = { it.id }
        ) { pl ->
            PlaylistRow(
                playlist = pl,
                imageLoader = imageLoader,
                onClick = { onPlaylistClick(pl) },
                titleColor = titleColor,
                hintColor = hintColor,
                rowHeight = dimensionResource(R.dimen.Settings_Height_61),
                rowPadding = dimensionResource(R.dimen.track_13),
                coverSize = dimensionResource(R.dimen.track_45),
                between = dimensionResource(R.dimen.Drawable_padding_8)
            )
        }
    }
}
