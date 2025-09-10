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
const val MENU_DIM = 0.70f   // затемнение, когда меню открыто

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

// для ImportPreviewFragment - импорт и просмотр и далее сохранение если будет выбор
const val ARG_IMPORT_URI     = "import_uri"        // входящий .plz/.zip Uri
const val ARG_PREFILL_NAME   = "prefill_name"
const val ARG_PREFILL_DESC   = "prefill_desc"
const val ARG_PREFILL_COVER  = "prefill_cover"     // String (content:// или file://)
const val ARG_PREFILL_TRACKS = "arg_prefill_tracks"

const val ACTION_SHOW_IMPORT_PREVIEW = "com.example.playlistmaker.SHOW_IMPORT_PREVIEW"
const val EXTRA_IMPORT_ENTRY = "extra_is_import_entry"
const val EXTRA_IMPORT_URI = "import_uri"

const val HEAD_READ_LIMIT = 128 * 1024 // 131072


