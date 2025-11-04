package com.example.playlistmaker.utils

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.core.net.toUri
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
import java.net.URLConnection
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

object PlaylistExport {

    private fun openInputStream(ctx: Context, pathOrUri: String): InputStream? = when {
        pathOrUri.startsWith("content://") || pathOrUri.startsWith("file://") ->
            runCatching { ctx.contentResolver.openInputStream(pathOrUri.toUri()) }.getOrNull()
        pathOrUri.startsWith("/") ->
            runCatching { FileInputStream(File(pathOrUri)) }.getOrNull()
        else -> null
    }

    private fun detectCoverEntryName(ctx: Context, pathOrUri: String): String {
        // 1) сначала по MIME (для content://)
        val mime = runCatching {
            if (pathOrUri.startsWith("content://")) {
                ctx.contentResolver.getType(pathOrUri.toUri())
            } else null
        }.getOrNull()

        val extFromMime = when (mime) {
            "image/png"  -> "png"
            "image/webp" -> "webp"
            "image/jpeg" -> "jpg"
            else -> null
        }
        if (extFromMime != null) return "cover.$extFromMime"

        // 2) по расширению строки
        val lower = pathOrUri.lowercase()
        return when {
            lower.endsWith(".png")  -> "cover.png"
            lower.endsWith(".webp") -> "cover.webp"
            lower.endsWith(".jpg") || lower.endsWith(".jpeg") -> "cover.jpg"
            else -> {
                // 3) финальный фолбэк — «угадай» по контенту
                val ext = runCatching {
                    openInputStream(ctx, pathOrUri)?.use { ins ->
                        URLConnection.guessContentTypeFromStream(ins)
                    }
                }.getOrNull()?.let {
                    when (it) {
                        "image/png"  -> "png"
                        "image/webp" -> "webp"
                        "image/jpeg" -> "jpg"
                        else -> null
                    }
                } ?: "jpg"
                "cover.$ext"
            }
        }
    }

    suspend fun exportToZip(
        ctx: Context,
        ui: com.example.playlistmaker.presentation.playlistInfo.PlaylistInfoViewModel.Ui
    ): Uri? = withContext(Dispatchers.IO) {

        // 1) Папка: cache/shared_exports (FileProvider должен разрешать cache-path)
        val dir = File(ctx.cacheDir, "shared_exports").apply { mkdirs() }

        // 2) Имя файла — именно .zip (НЕ .plz)
        val safeName = ui.name.ifBlank { "playlist" }.replace(Regex("[^\\w\\-]"), "_")
        val zipFile = File(dir, "${safeName}_${System.currentTimeMillis()}.zip")

        // 3) DTO
        val coverPath = ui.coverPath?.takeIf { it.isNotBlank() }
        val coverNameInZip = coverPath?.let { detectCoverEntryName(ctx, it) }

        val dto = com.example.playlistmaker.data.dto.share_album.SharedPlaylistDto(
            name = ui.name,
            description = ui.description,
            coverFileName = coverNameInZip,
            tracks = ui.tracks.map(Track::toShared)
        )

        // 4) ZIP
        ZipOutputStream(BufferedOutputStream(FileOutputStream(zipFile))).use { zos ->
            // playlist.json
            val json = Gson().toJson(dto)
            zos.putNextEntry(ZipEntry("playlist.json"))
            zos.write(json.toByteArray(Charsets.UTF_8))
            zos.closeEntry()

            // cover (опционально)
            if (coverPath != null) {
                openInputStream(ctx, coverPath)?.use { ins ->
                    val name = coverNameInZip ?: "cover.jpg"
                    zos.putNextEntry(ZipEntry(name))
                    ins.copyTo(zos)
                    zos.closeEntry()
                }
                // если не удалось открыть обложку — просто идём дальше без неё
            }
            zos.flush()
        }

        // 5) content:// для шаринга
        FileProvider.getUriForFile(ctx, "${ctx.packageName}.fileprovider", zipFile)
    }
}
