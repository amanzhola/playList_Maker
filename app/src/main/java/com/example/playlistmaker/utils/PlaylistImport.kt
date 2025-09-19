package com.example.playlistmaker.utils

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.playlistmaker.data.dto.share_album.SharedPlaylistDto
import com.example.playlistmaker.data.dto.share_album.SharedTrackDto
import com.google.gson.Gson
import java.io.BufferedInputStream
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

object PlaylistImport {

    data class ImportedPlaylist(
        val name: String,
        val description: String?,
        val coverUri: Uri?,              // временный Uri на картинку в кеше
        val tracks: List<SharedTrackDto> // твой DTO
    )

    fun parse(ctx: Context, uri: Uri): ImportedPlaylist {
        ctx.contentResolver.openInputStream(uri).use { ins ->
            requireNotNull(ins) { "Не удалось открыть файл" }

            // Пробуем как ZIP
            return ZipInputStream(BufferedInputStream(ins)).use { zis ->
                var entry: ZipEntry?
                var json: String? = null
                var coverTempFile: File? = null

                val dir = File(ctx.cacheDir, "import_preview").apply { mkdirs() }

                while (true) {
                    entry = zis.nextEntry ?: break

                    val name = entry.name.lowercase()
                    when {
                        name.endsWith("playlist.json") -> {
                            json = zis.bufferedReader(Charsets.UTF_8).readText()
                        }
                        name.endsWith(".png") || name.endsWith(".jpg")
                                || name.endsWith(".jpeg") || name.endsWith(".webp") -> {
                            val out = File(dir, "cover_${System.currentTimeMillis()}_${name.substringAfterLast('/')}")
                            out.outputStream().use { zos -> zis.copyTo(zos) }
                            coverTempFile = out
                        }
                    }
                    zis.closeEntry()
                }

                // Если не похоже на ZIP (нет записей) — fallback как plain JSON
                if (json == null) {
                    // переоткроем входной поток и попробуем прочесть текст
                    ctx.contentResolver.openInputStream(uri).use { re ->
                        json = re?.bufferedReader(Charsets.UTF_8)?.readText()
                    }
                }

                require(!json.isNullOrBlank()) { "playlist.json не найден" }

                val dto = Gson().fromJson(json, SharedPlaylistDto::class.java)
                val coverUri = coverTempFile?.let { FileProvider.getUriForFile(
                    ctx, "${ctx.packageName}.fileprovider", it
                )}

                ImportedPlaylist(
                    name = dto.name,
                    description = dto.description,
                    coverUri = coverUri,
                    tracks = dto.tracks
                )
            }
        }
    }
}
