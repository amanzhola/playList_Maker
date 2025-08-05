package com.example.playlistmaker.domain.models.search

import android.os.Parcelable
import com.example.playlistmaker.utils.Identifiable
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
    var playTime: String? = "0:00",
    var isFavorite: Boolean = false
//    var playTime: String? = "🕒0:00" // ❌ (☝️ for OK people)
): Parcelable, Identifiable<Int> {

    override val id: Int
        get() = trackId

    val trackDuration: String // 🧪
        get() = SimpleDateFormat("mm:ss", Locale.getDefault()).format(trackTimeMillis)

    val artworkUrlSmall: String // 🎁
        get() = artworkUrl100 ?: ""

    val artworkUrl512: String // ❓
        get() = artworkUrl100?.replaceAfterLast("/", "512x512bb.jpg") ?: ""
}