package com.example.playlistmaker.utils

import android.net.Uri
import androidx.core.net.toUri
import java.io.File

/** Преобразует строковый путь/uri в модель для Glide/Coil. */
fun coverModelFrom(path: String?): Any? =
    path?.takeIf { it.isNotBlank() }?.let { ref ->
        when {
            ref.startsWith("content://") || ref.startsWith("file://") -> ref.toUri()
            ref.startsWith("/")  -> java.io.File(ref)
            ref.startsWith("http")-> ref
            else -> null
        }
    }
// for PlaylistRow.kt
fun parseCoverUri(coverPath: String?): Uri? = coverPath?.let { ref ->
    when {
        ref.startsWith("content://") || ref.startsWith("file://") -> ref.toUri()
        ref.startsWith("/") -> Uri.fromFile(File(ref))
        else -> null
    }
}
