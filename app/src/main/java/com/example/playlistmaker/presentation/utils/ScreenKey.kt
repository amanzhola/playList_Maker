package com.example.playlistmaker.presentation.utils

import androidx.fragment.app.Fragment
import com.example.playlistmaker.ui.createPlaylist.CreatePlaylistFragment
import com.example.playlistmaker.ui.mediaFragments.FragmentFavouriteTracks
import com.example.playlistmaker.ui.mediaFragments.FragmentPlaylist
import com.example.playlistmaker.utils.ARG_EDIT_ID

/**
 * Возвращает стабильный ключ экрана для сохранения цветов.
 * Здесь можно кастомизировать особые экраны/табы.
 */
fun Fragment.screenKeyOrDefault(): String {
    // Сначала обрабатываем CreatePlaylistFragment без when
    (this as? CreatePlaylistFragment)?.let { cp ->
        val args = cp.arguments
        val isEdit = args?.containsKey(ARG_EDIT_ID) == true
        return if (isEdit) {
            val id = args?.getLong(ARG_EDIT_ID, -1L)?.takeIf { it > 0L } ?: -1L
            "CreatePlaylist:edit:$id"
        } else {
            "CreatePlaylist:new"
        }
    }

    // Остальные случаи — как и было
    return when (this) {
        is FragmentFavouriteTracks -> "MediaTab:Favourite"
        is FragmentPlaylist       -> "MediaTab:Playlist"
        else -> this::class.java.simpleName
    }
}

