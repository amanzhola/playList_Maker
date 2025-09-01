package com.example.playlistmaker.utils

const val SEARCH_DEBOUNCE_DELAY = 2000L
//const val CLICK_DEBOUNCE_DELAY = 1000L // ⛔ 🕒 1 секунда задержки
const val CLICK_DEBOUNCE_DELAY = 300L // ⛔ 🕒 1 секунда задержки

object NavKeys {
    const val PLAYLIST_CREATED_NAME = "playlist_created_name"
    const val SCROLL_TOP = "scroll_top"
    const val SELECT_TAB = "select_tab"
}

const val ACTION_SHARE = 1
const val ACTION_EDIT  = 2
const val ACTION_DELETE= 3

const val BASE_DIM = 0.35f   // затемнение, когда меню закрыто
const val MENU_DIM = 0.90f   // затемнение, когда меню открыто

// для CreatePlaylist - редактирование
const val ARG_EDIT_ID    = "edit_id"
const val ARG_EDIT_NAME  = "edit_name"
const val ARG_EDIT_DESC  = "edit_desc"
const val ARG_EDIT_COVER = "edit_cover"

// для CreatePlaylist - редактирование
const val UPDATE_METADATA_SQL = """
    UPDATE playlists
    SET name = :name,
        description = :description,
        coverPath = :coverPath
    WHERE id = :id
"""