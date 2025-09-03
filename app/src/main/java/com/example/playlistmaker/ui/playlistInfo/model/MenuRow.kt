package com.example.playlistmaker.ui.playlistInfo.model

// models
sealed class MenuRow {
    data class Header(val cover: Any?, val name: String, val count: String) : MenuRow()
    data class Action(val id: Int, val title: String) : MenuRow()
}