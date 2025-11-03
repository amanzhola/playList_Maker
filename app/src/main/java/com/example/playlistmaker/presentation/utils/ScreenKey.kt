package com.example.playlistmaker.presentation.utils

import androidx.fragment.app.Fragment
import com.example.playlistmaker.ui.createPlaylist.CreatePlaylistFragment
import com.example.playlistmaker.ui.media.MediaActiveTabProvider
import com.example.playlistmaker.utils.ARG_EDIT_ID
import com.example.playlistmaker.utils.SCOPE_FAV
import com.example.playlistmaker.utils.SCOPE_PL

fun Fragment.screenKeyOrDefault(): String {
    // 1) Спец-случай для CreatePlaylist (как у тебя было)
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

    // 2) Медиа экран: ключ вкладки определяется текущим табом контейнера
    findMediaTabProvider()?.let { provider ->
        return when (provider.currentMediaTab()) {
            0 -> SCOPE_FAV       // "MediaTab:Favourite"
            1 -> SCOPE_PL        // "MediaTab:Playlist"
            else -> "MediaTab:Unknown"
        }
    }

    // 3) Остальные — по умолчанию
    return this::class.java.simpleName
}

/** Ищем, кто умеет сказать текущий таб: сам фрагмент или его родитель */
private fun Fragment.findMediaTabProvider(): MediaActiveTabProvider? =
    when {
        this is MediaActiveTabProvider -> this
        parentFragment is MediaActiveTabProvider -> parentFragment as MediaActiveTabProvider
        // если контейнер ещё глубже — можно расширить через requireActivity().supportFragmentManager и т.п.
        else -> null
    }
