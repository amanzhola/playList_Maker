package com.example.playlistmaker.ui.launcher

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.playlistmaker.domain.api.base.TrackSerializer
import com.example.playlistmaker.domain.api.base.TrackStorageHelper
import com.example.playlistmaker.domain.api.movie.MovieSerializer
import com.example.playlistmaker.domain.api.movie.MovieStorageHelper
import com.example.playlistmaker.domain.models.movie.Movie
import com.example.playlistmaker.domain.models.search.Track
import com.example.playlistmaker.ui.launcherPosters.TrackDetailActivity
import com.example.playlistmaker.ui.moviePosters.MoviePager
import org.koin.android.ext.android.inject
import java.io.FileNotFoundException
import java.io.IOException

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
        when (intent?.action) {
            Intent.ACTION_SEND -> {
                if (intent.type == "text/plain") {
                    val sharedText = intent.getStringExtra(Intent.EXTRA_TEXT)
                    if (!sharedText.isNullOrEmpty()) {
                        handleTrackFromText(sharedText)
                    } else {
                        "Ошибка".showAlertDialog("Получен пустой текст.") // 😕
                    }
                }
            }

            Intent.ACTION_VIEW -> {
                val uri: Uri? = intent.data
                if (uri != null) {
                    handleTrackFromUri(uri)
                } else {
                    "Ошибка".showAlertDialog("Некорректный Uri.") // 🤔 😬
                }
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
}