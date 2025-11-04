package com.example.playlistmaker.utils

import android.annotation.SuppressLint
import android.net.Uri
import androidx.core.net.toUri
import java.io.File

/** Преобразует строковый путь/uri в модель для Glide/Coil. */
fun coverModelFrom(path: String?): Any? =
    path?.takeIf { it.isNotBlank() }?.let { ref ->
        when {
            ref.startsWith("content://") || ref.startsWith("file://") -> ref.toUri()
            ref.startsWith("/")  -> File(ref)
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

// for compose ones Audio, PlaylistInfo and PlaylistEdit
@SuppressLint("UseKtx")
fun coverModelFromPath(path: String?): Any? {
    val p = path?.trim().orEmpty()
    if (p.isEmpty()) return null
    return when {
        p.startsWith("content://") || p.startsWith("file://") -> Uri.parse(p)
        p.startsWith("/") -> File(p)           // абсолютный путь
        else -> null
    }
}

// for menu sheet
@SuppressLint("UseKtx")
fun coverModelFromAny(src: Any?): Any? = when (src) {
    null -> null
    is Uri  -> src
    is File -> src
    is String -> {
        val p = src.trim()
        when {
            p.isEmpty() -> null
            p.startsWith("content://") || p.startsWith("file://") -> Uri.parse(p)
            p.startsWith("/") -> File(p)                    // абсолютный путь
            else -> null
        }
    }
    else -> null
}

