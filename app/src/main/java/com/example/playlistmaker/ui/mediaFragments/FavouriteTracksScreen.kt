package com.example.playlistmaker.ui.mediaFragments

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.playlistmaker.R
import com.example.playlistmaker.domain.models.search.Track
import com.example.playlistmaker.presentation.media.FavoriteTracksViewModel
import com.example.playlistmaker.utils.FailBlock
import com.example.playlistmaker.utils.FailTextPlacement
import com.example.playlistmaker.utils.TrackRow
import com.google.gson.Gson
import org.koin.androidx.compose.koinViewModel

@Composable
fun FavouriteTracksScreen(
    vm: FavoriteTracksViewModel = koinViewModel(),
    onOpenTrack: (track: Track, listJson: String, index: Int) -> Unit,
    screenBackgroundOverride: Color? = null,
    rowTextColorOverride: Color? = null,
    rowIconColorOverride: Color? = null,
    rowBackgroundOverride: Color? = null
) {
    val state by vm.state.collectAsStateWithLifecycle()
    val bg = screenBackgroundOverride ?: colorResource(R.color.white_textColor)

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                vm.reloadFavorites()   // перезагрузим избранное при возврате на экран
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    when {
        state.isLoading -> Unit
        state.isEmpty -> {
            FailBlock(
                textId = R.string.emptyMedia,
                enabled = true,
                textPlacement = FailTextPlacement.Bottom,
                topPadding = dimensionResource(R.dimen.icon_size)
            )
        }

        else -> {
            val tracks = state.tracks
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(bg)
            ) {
                items(tracks, key = { it.trackId }) { t ->
                    TrackRow(
                        track = t,
                        onClick = {
                            val json = Gson().toJson(tracks)
                            val idx = tracks.indexOfFirst { it.trackId == t.trackId }
                                .let { if (it >= 0) it else 0 }
                            onOpenTrack(t, json, idx)
                        },
//                        onRemove = { /* избранное: не нужно */ },
                        // дергаем уже существующий триггер VM
                        onRemove = {
                            vm.reloadFavorites()   // перезапросит/переэмичит favoritesFlow -> список сам обновится
                        },
                        // ← подставляем оверрайды (если не null)
                        nameColorOverride          = rowTextColorOverride,
                        arrowColorOverride         = rowIconColorOverride,
                        rowBackgroundColorOverride = rowBackgroundOverride
                    )
                }
            }
        }
    }
}
