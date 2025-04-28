package com.example.playlistmaker.search

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.text.SimpleDateFormat
import java.util.Locale

@Parcelize
data class Track( // 🧱
    val trackName: String,// 🎵
    val artistName: String,// 🎤
    val trackTimeMillis: Long, // ⏱️
    val artworkUrl100: String?, // 💿
    val collectionName: String, // 📀
    val releaseDate: String, // 📅
    val primaryGenreName: String, // 📚
    val country: String, // 🌍
    val previewUrl: String, // 🎧
    val trackId: Int, // 🆔
    var isPlaying: Boolean = false,
    var playTime: String? = "🕒0:00"
): Parcelable {
    val trackDuration: String // 🧪
        get() = SimpleDateFormat("mm:ss", Locale.getDefault()).format(trackTimeMillis)

    val artworkUrlSmall: String // 🎁
        get() = artworkUrl100 ?: ""

    val artworkUrl512: String // ❓
        get() = artworkUrl100?.replaceAfterLast("/", "512x512bb.jpg") ?: ""
}