package com.example.playlistmaker.presentation

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// не используется но для сохранения в общей хранинилище (дулирует файлы в общей хранилище)
object MediaStoreSaver_public {

    suspend fun saveCoverToPublicMedia(
        context: Context,
        src: Uri
    ): Uri? = withContext(Dispatchers.IO) {
        val r = context.contentResolver

        // 1) Определяем MIME исходника, подбираем расширение
        val srcMime = r.getType(src) ?: "image/jpeg"
        val ext = when (srcMime.lowercase()) {
            "image/png"  -> "png"
            "image/webp" -> "webp"
            else         -> "jpg"
        }

        val name = "playlist_cover_${System.currentTimeMillis()}.$ext"

        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, name)
            put(MediaStore.Images.Media.MIME_TYPE, srcMime)                   // ← правильный MIME
            put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/PlaylistMaker")
            put(MediaStore.Images.Media.IS_PENDING, 1)
        }

        val dst = r.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
            ?: return@withContext null

        try {
            r.openInputStream(src).use { input ->
                r.openOutputStream(dst, "w").use { output ->                 // ← явный "w"
                    if (input == null || output == null) return@withContext null
                    input.copyTo(output)
                    output.flush()
                }
            }

            // 2) Сбрасываем pending
            r.update(dst, ContentValues().apply {
                put(MediaStore.Images.Media.IS_PENDING, 0)
            }, null, null)

            // 3) Быстрая валидация: можно ли читать то, что записали?
            r.openInputStream(dst)?.use { /* если открылся — всё ок */ } ?: return@withContext null

            dst
        } catch (t: Throwable) {
            r.delete(dst, null, null)
            null
        }
    }
}
