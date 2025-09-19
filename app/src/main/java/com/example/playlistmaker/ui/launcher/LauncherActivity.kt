package com.example.playlistmaker.ui.launcher

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.JsonReader
import android.util.JsonToken
import androidx.appcompat.app.AppCompatActivity
import com.example.playlistmaker.domain.api.base.TrackSerializer
import com.example.playlistmaker.domain.api.base.TrackStorageHelper
import com.example.playlistmaker.domain.api.movie.MovieSerializer
import com.example.playlistmaker.domain.api.movie.MovieStorageHelper
import com.example.playlistmaker.domain.models.movie.Movie
import com.example.playlistmaker.domain.models.search.Track
import com.example.playlistmaker.roots.main.MainActivity
import com.example.playlistmaker.ui.launcherPosters.TrackDetailActivity
import com.example.playlistmaker.ui.movie.moviePosters.MoviePager
import com.example.playlistmaker.utils.ACTION_SHOW_IMPORT_PREVIEW
import com.example.playlistmaker.utils.EXTRA_IMPORT_ENTRY
import com.example.playlistmaker.utils.EXTRA_IMPORT_URI
import org.koin.android.ext.android.inject
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStreamReader

class LauncherActivity : AppCompatActivity() {

    private val trackStorageHelper: TrackStorageHelper by inject() // 👉 📦
    private val movieStorageHelper: MovieStorageHelper by inject() // 👉 📦
    private val trackSerializer: TrackSerializer by inject() // 🎶 ↔️ 🎵
    private val movieSerializer: MovieSerializer by inject() // 🎥💃 ↔️ 💃🎬

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleIncomingIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIncomingIntent(intent)
    }

    private fun handleIncomingIntent(intent: Intent?) {
        if (intent == null) { finish(); return }

        when (intent.action) {
            Intent.ACTION_SEND -> {
                when (intent.type) {
                    "text/plain" -> {
                        val sharedText = intent.getStringExtra(Intent.EXTRA_TEXT)
                        if (!sharedText.isNullOrEmpty()) handleTrackFromText(sharedText)
                        else "Ошибка".showAlertDialog("Получен пустой текст.") // 😕
                    }
                    else -> {
                        // Любой файл (zip/json/unknown) — решаем по содержимому
                        val uri = extractZipUri(intent)
                        if (uri == null) { finish(); return }
                        routeByContent(uri, intent.type)
                    }
                }
            }

            Intent.ACTION_VIEW -> {
                val uri = intent.data ?: intent.clipData?.getItemAt(0)?.uri
                if (uri == null) { finish(); return }
                routeByContent(uri, intent.type)
            }

            else -> finish()
        }
    }

    private fun handleTrackFromText(sharedText: String) {

        try {
            val tracks = trackSerializer.deserializeList(sharedText)
            if (!tracks.isNullOrEmpty()) {
                0.openTrackDetail(tracks.toTypedArray())
            } else {
                "Ошибка".showAlertDialog("Список треков пуст.")  // 😕
            }
        } catch (e: Exception) { // 🤔 😬
            "Ошибка".showAlertDialog("Не удалось обработать трек: ${e.message}") // 😌
        }
    }

    // 🔐 🔜 🧱 (Чистая архитектура) 🛡️ (Безопасность) 🔁 (Обратная совместимость)
    private fun handleTrackFromUri(uri: Uri) { // 💃 🌼
        try {
            val inputStream = contentResolver.openInputStream(uri)
            val json = inputStream?.bufferedReader().use { it?.readText() }

            if (json.isNullOrEmpty()) {
                "Ошибка".showAlertDialog("Получен пустой JSON-файл.") // 😕
                return
            }

            // 2. Пробуем как один фильм 🔙
            try { // ❓ 🔜  📽️🍿💃
                val movie = movieSerializer.deserialize(json)
                if (movie != null) {
                    0.openMovieDetail(arrayOf(movie))
                    return
                }
            } catch (_: Exception) {}

            // 1. Пробуем как список треков
            try { // ❓ 🔜  🎧 🎵 💿 ↔️ 📀 🎶
                val trackList = trackSerializer.deserializeList(json)
                if (!trackList.isNullOrEmpty()) {
                    0.openTrackDetail(trackList.toTypedArray())
                    return
                }
            } catch (_: Exception) {}

            // 3. Пробуем как список фильмов (на будущее) 🎥️🚀
            try { // ❓ 🔜  📽️+🎥+🎬 🔚 ✨💃
                val movieList = movieSerializer.deserializeList(json)
                if (!movieList.isNullOrEmpty()) {
                    0.openMovieDetail(movieList.toTypedArray())
                    return
                }
            } catch (_: Exception) {}

            "Ошибка".showAlertDialog("Не удалось распознать содержимое файла.") // 🤔 😬
        } catch (e: FileNotFoundException) {
            "Ошибка".showAlertDialog("Файл не найден.") // 😕
        } catch (e: IOException) {
            "Ошибка".showAlertDialog("Ошибка чтения JSON-файла.") // 😌
        } catch (e: Exception) {
            "Ошибка".showAlertDialog("Произошла ошибка при обработке Uri.") // ❌
        }
    }

    private fun String.showAlertDialog(message: String) { //  👨‍💻✨
        AlertDialog.Builder(this@LauncherActivity)
            .setTitle(this)
            .setMessage(message)
            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun Int.openTrackDetail(tracks: Array<Track>) { // 👌 😉 🎵
        trackStorageHelper.saveTrackList(tracks.toList())  // ⬅️ 🎶 📜 👉 📝 📦 💾 (сохраняем список)
        trackStorageHelper.setCurrentIndex(this)     // ⬅️ 🎵 📜 👉 📝 📦 💾 (сохраняем индекс)

        val intent = Intent(this@LauncherActivity, TrackDetailActivity::class.java)
        startActivity(intent)
        finish()
    } // provideTrackStorageHelper shows fail -> see TrackAdapter newFiles  💥

    private fun Int.openMovieDetail(movies: Array<Movie>) { // 👌 😉 📽️
        val selectedMovie = movies[this]

        movieStorageHelper.saveMovie(selectedMovie)

        val intent = Intent(this@LauncherActivity, MoviePager::class.java)
        startActivity(intent)
        finish()
    } // provideTrackStorageHelper shows fail -> see TrackAdapter newFiles  💥

    // Перекидываем в MainActivity
    private fun forwardToImportPreview(uri: Uri) {
        val i = Intent(this, MainActivity::class.java).apply {
            action = ACTION_SHOW_IMPORT_PREVIEW
            putExtra(EXTRA_IMPORT_URI, uri)
            putExtra(EXTRA_IMPORT_ENTRY, true) // помечаем «вошли извне»
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }
        startActivity(i)
        finish()
    }
    //*******************************************************************************
    // album preview ****************************************************************

    // универсальный извлекатель
    private fun extractZipUri(intent: Intent): Uri? {
        return when (intent.action) {
            Intent.ACTION_SEND -> {
                // ZIP чаще всего в EXTRA_STREAM
                val fromExtra = androidx.core.os.BundleCompat.getParcelable(
                    intent.extras ?: return null, Intent.EXTRA_STREAM, Uri::class.java
                )
                fromExtra ?: intent.clipData?.getItemAt(0)?.uri
            }
            Intent.ACTION_VIEW -> {
                intent.data ?: intent.clipData?.getItemAt(0)?.uri
            }
            Intent.ACTION_SEND_MULTIPLE -> {
                intent.clipData?.getItemAt(0)?.uri
            }
            else -> null
        }
    }

    /** Главная развилка: импорт vs треки/кино */
    private fun routeByContent(uri: Uri, mime: String?) {
        try {
            // 1) ZIP-пакет с playlist.json?
            if (looksLikeZip(mime, uri) && hasPlaylistJson(uri)) {
                forwardToImportPreview(uri); return
            }

            // 2) Возможно обычный JSON-плейлист (без zip)
            if (isPlaylistJson(uri)) {
                forwardToImportPreview(uri); return
            }

            // 3) Иначе — старый поток (трек/кино)
            handleTrackFromUri(uri)

        } catch (_: Exception) {
            // Если что-то пошло не так, пробуем старую логику (не падаем)
            handleTrackFromUri(uri)
        }
    }

    /** Быстро проверяем, похоже ли на ZIP: либо тип, либо сигнатура 'PK' */
    private fun looksLikeZip(mime: String?, uri: Uri): Boolean {
        val t = mime?.lowercase()
        val name = uri.lastPathSegment?.lowercase() ?: ""

        if (t == "application/zip" || t == "application/x-zip-compressed") return true
        if (t == "application/octet-stream" && (name.endsWith(".zip") || name.endsWith(".plz"))) return true
        if (name.endsWith(".zip") || name.endsWith(".plz")) return true

        // пробуем сигнатуру 'PK'
        return probeZipHeader(uri)
    }

    /** Читаем первые 4 байта и ищем 'P''K' */
    private fun probeZipHeader(uri: Uri): Boolean {
        return try {
            contentResolver.openInputStream(uri)?.use { ins ->
                val sig = ByteArray(4)
                val n = ins.read(sig)
                n >= 2 && sig[0] == 0x50.toByte() && sig[1] == 0x4B.toByte()
            } ?: false
        } catch (_: Exception) { false }
    }

    /** Открываем как Zip и быстро проверяем наличие playlist.json (без чтения всего файла) */
    private fun hasPlaylistJson(uri: Uri): Boolean {
        return try {
            contentResolver.openInputStream(uri)?.use { base ->
                java.util.zip.ZipInputStream(base).use { zis ->
                    var e: java.util.zip.ZipEntry?
                    var found = false
                    while (true) {
                        e = zis.nextEntry ?: break
                        val n = e.name.lowercase()
                        if (n.endsWith("playlist.json")) { found = true; break }
                        zis.closeEntry()
                    }
                    found
                }
            } ?: false
        } catch (_: Exception) { false }
    }

    /** Это плейлист-JSON (объект с "tracks"), а не массив треков? */
    private fun isPlaylistJson(uri: Uri): Boolean {
        return try {
            contentResolver.openInputStream(uri)?.use { ins ->
                JsonReader(InputStreamReader(ins, Charsets.UTF_8)).use { r ->
                    r.isLenient = true
                    if (r.peek() != JsonToken.BEGIN_OBJECT) return false

                    var hasName = false
                    var hasTracksArray = false
                    var firstTrackLooksValid = false

                    r.beginObject()
                    while (r.hasNext()) {
                        when (r.nextName()) {
                            "name" -> {
                                hasName = (r.peek() == JsonToken.STRING)
                                r.skipValue()
                            }
                            "tracks" -> {
                                if (r.peek() == JsonToken.BEGIN_ARRAY) {
                                    hasTracksArray = true
                                    r.beginArray()
                                    // Заглянем в первый элемент массива и проверим «похожесть» на наш SharedTrackDto
                                    if (r.hasNext() && r.peek() == JsonToken.BEGIN_OBJECT) {
                                        r.beginObject()
                                        var hasTrackName = false
                                        var hasArtistName = false
                                        var hasTrackId = false
                                        // посмотрим пару первых полей
                                        repeat(10) {
                                            if (!r.hasNext() || r.peek() == JsonToken.END_OBJECT) return@repeat
                                            val n = r.nextName()
                                            when (n) {
                                                "trackName"   -> { hasTrackName = (r.peek() == JsonToken.STRING); r.skipValue() }
                                                "artistName"  -> { hasArtistName = (r.peek() == JsonToken.STRING); r.skipValue() }
                                                "trackId"     -> { hasTrackId = (r.peek() == JsonToken.NUMBER); r.skipValue() }
                                                else          -> r.skipValue()
                                            }
                                        }
                                        // для надёжности: либо есть trackId, либо пара trackName+artistName
                                        firstTrackLooksValid = hasTrackId || (hasTrackName && hasArtistName)
                                        // дочитаем объект до конца
                                        while (r.hasNext()) r.skipValue()
                                        r.endObject()
                                    }
                                    // дочитаем остаток массива быстро
                                    while (r.hasNext()) r.skipValue()
                                    r.endArray()
                                } else {
                                    r.skipValue()
                                }
                            }
                            else -> r.skipValue()
                        }
                    }
                    r.endObject()

                    hasName && hasTracksArray && firstTrackLooksValid
                }
            } ?: false
        } catch (_: Exception) {
            false
        }
    }
}