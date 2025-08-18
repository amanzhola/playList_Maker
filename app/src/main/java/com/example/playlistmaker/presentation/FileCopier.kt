package com.example.playlistmaker.presentation

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

object FileCopier {
    fun copyToAppStorage(context: Context, src: Uri): String? = try {
        val resolver = context.contentResolver
        val ext = resolver.getType(src)?.substringAfterLast('/') ?: "jpg"
        val dir = File(context.filesDir, "playlist_covers").apply { mkdirs() }
        val dst = File(dir, "${UUID.randomUUID()}.$ext")
        resolver.openInputStream(src).use { input ->
            FileOutputStream(dst).use { output -> input?.copyTo(output) }
        }
        dst.absolutePath
    } catch (_: Exception) { null }
}
