package com.example.playlistmaker.utils

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import com.example.playlistmaker.data.dto.share_album.SharedPlaylistDto
import com.example.playlistmaker.data.mappers.toShared
import com.example.playlistmaker.domain.models.search.Track
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

object PlaylistExport {

    // ui.coverPath может быть file://, content:// или обычный абсолютный путь
    private fun openInputStream(ctx: Context, pathOrUri: String): InputStream? {
        return when {
            pathOrUri.startsWith("content://") || pathOrUri.startsWith("file://") ->
                runCatching { ctx.contentResolver.openInputStream(pathOrUri.toUri()) }.getOrNull()
            pathOrUri.startsWith("/") ->
                runCatching { FileInputStream(File(pathOrUri)) }.getOrNull()
            else -> null
        }
    }

    suspend fun exportToZip(
        ctx: Context,
        ui: com.example.playlistmaker.presentation.playlistInfo.PlaylistInfoViewModel.Ui
    ): Uri? = withContext(Dispatchers.IO) {

        // 1) Куда пишем: cache/shared_exports (FileProvider уже покрывает cache-path)
        val dir = File(ctx.cacheDir, "shared_exports").apply { mkdirs() }

        // 2) Имя файла (добавим timestamp, чтобы не затирать предыдущие)
        val safeName = ui.name.ifBlank { "playlist" }.replace(Regex("[^\\w\\-]"), "_")
        val zipFile = File(dir, "${safeName}_${System.currentTimeMillis()}.plz")

        // 3) DTO для JSON
        // coverPath (может быть null/пустой) + coverName (если есть путь)
        val coverPath = ui.coverPath?.takeIf { it.isNotBlank() }
        val coverName = coverPath?.let { coverFileNameFromPath(it) }

        val dto = SharedPlaylistDto(
            name = ui.name,
            description = ui.description,
            coverFileName = coverName, // имя файла в zip, если будет обложка
            tracks = ui.tracks.map(Track::toShared)
        )

        // 4) Пишем ZIP
        ZipOutputStream(BufferedOutputStream(FileOutputStream(zipFile))).use { zos ->
            // playlist.json
            val json = Gson().toJson(dto)
            zos.putNextEntry(ZipEntry("playlist.json"))
            zos.write(json.toByteArray(Charsets.UTF_8))
            zos.closeEntry()

            // cover (если есть). Никаких лишних проверок — всё через безопасные let’ы.
            coverPath?.let { path ->
                val name = coverFileNameFromPath(path) // можно и coverName!!, но так безопаснее
                openInputStream(ctx, path)?.use { ins ->
                    zos.putNextEntry(ZipEntry(name))
                    ins.copyTo(zos)
                    zos.closeEntry()
                }
            }
        }

        // 5) Отдаём content:// через FileProvider
        FileProvider.getUriForFile(ctx,"${ctx.packageName}.fileprovider",zipFile)
    }

    // 3) DTO для JSON
    private fun coverFileNameFromPath(path: String): String = when {
        path.endsWith(".png",  true) -> "cover.png"
        path.endsWith(".webp", true) -> "cover.webp"
        else                         -> "cover.jpg"
    }

}
