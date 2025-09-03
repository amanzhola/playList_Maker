package com.example.playlistmaker.utils

import androidx.core.net.toUri

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
