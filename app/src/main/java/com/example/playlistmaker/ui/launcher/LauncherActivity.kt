package com.example.playlistmaker.ui.launcher

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.playlistmaker.creator.Creator
import com.example.playlistmaker.domain.models.Movie
import com.example.playlistmaker.domain.models.Track
import com.example.playlistmaker.ui.launcherPosters.TrackDetailActivity
import com.example.playlistmaker.ui.moviePosters.MoviePager
import java.io.FileNotFoundException
import java.io.IOException

class LauncherActivity : AppCompatActivity() {

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
                        showAlertDialog("Ошибка", "Получен пустой текст.") // 😕
                    }
                }
            }

            Intent.ACTION_VIEW -> {
                val uri: Uri? = intent.data
                if (uri != null) {
                    handleTrackFromUri(uri)
                } else {
                    showAlertDialog("Ошибка", "Некорректный Uri.") // 🤔 😬
                }
            }

            else -> finish()
        }
    }

    private fun handleTrackFromText(sharedText: String) {
        val trackSerializer = Creator.provideTrackSerializer()

        try {
            val tracks = trackSerializer.deserializeList(sharedText)
            if (!tracks.isNullOrEmpty()) {
                openTrackDetail(tracks.toTypedArray(), 0)
            } else {
                showAlertDialog("Ошибка", "Список треков пуст.")  // 😕
            }
        } catch (e: Exception) { // 🤔 😬
            showAlertDialog("Ошибка", "Не удалось обработать трек: ${e.message}") // 😌
        }
    }

    // 🔐 🔜 🧱 (Чистая архитектура) 🛡️ (Безопасность) 🔁 (Обратная совместимость)
    private fun handleTrackFromUri(uri: Uri) { // 💃 🌼
        try {
            val inputStream = contentResolver.openInputStream(uri)
            val json = inputStream?.bufferedReader().use { it?.readText() }

            if (json.isNullOrEmpty()) {
                showAlertDialog("Ошибка", "Получен пустой JSON-файл.") // 😕
                return
            }

            val trackSerializer = Creator.provideTrackSerializer()
            val movieSerializer = Creator.provideMovieSerializer()

            // 2. Пробуем как один фильм 🔙
            try { // ❓ 🔜  📽️🍿💃
                val movie = movieSerializer.deserialize(json)
                if (movie != null) {
                    openMovieDetail(arrayOf(movie), 0)
                    return
                }
            } catch (_: Exception) {}

            // 1. Пробуем как список треков
            try { // ❓ 🔜  🎧 🎵 💿 ↔️ 📀 🎶
                val trackList = trackSerializer.deserializeList(json)
                if (!trackList.isNullOrEmpty()) {
                    openTrackDetail(trackList.toTypedArray(), 0)
                    return
                }
            } catch (_: Exception) {}

            // 3. Пробуем как список фильмов (на будущее) 🎥️🚀
            try { // ❓ 🔜  📽️+🎥+🎬 🔚 ✨💃
                val movieList = movieSerializer.deserializeList(json)
                if (!movieList.isNullOrEmpty()) {
                    openMovieDetail(movieList.toTypedArray(), 0)
                    return
                }
            } catch (_: Exception) {}

            showAlertDialog("Ошибка", "Не удалось распознать содержимое файла.") // 🤔 😬
        } catch (e: FileNotFoundException) {
            showAlertDialog("Ошибка", "Файл не найден.") // 😕
        } catch (e: IOException) {
            showAlertDialog("Ошибка", "Ошибка чтения JSON-файла.") // 😌
        } catch (e: Exception) {
            showAlertDialog("Ошибка", "Произошла ошибка при обработке Uri.") // ❌
        }
    }

    private fun showAlertDialog(title: String, message: String) { //  👨‍💻✨
        AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun openTrackDetail(tracks: Array<Track>, trackIndex: Int) { // 👌 😉 🎵
        val trackStorageHelper = Creator.provideTrackStorageHelper(this)
        trackStorageHelper.saveTrackList(tracks.toList())  // ⬅️ 🎶 📜 👉 📝 📦 💾 (сохраняем список)
        trackStorageHelper.setCurrentIndex(trackIndex)     // ⬅️ 🎵 📜 👉 📝 📦 💾 (сохраняем индекс)

        val intent = Intent(this, TrackDetailActivity::class.java)
        startActivity(intent)
        finish()
    } // provideTrackStorageHelper shows fail -> see TrackAdapter newFiles  💥

    private fun openMovieDetail(movies: Array<Movie>, index: Int) { // 👌 😉 📽️
        val selectedMovie = movies[index]

        val movieStorageHelper = Creator.provideMovieStorageHelper(this)
        movieStorageHelper.saveMovie(selectedMovie)

        val intent = Intent(this, MoviePager::class.java)
        startActivity(intent)
        finish()
    } // provideTrackStorageHelper shows fail -> see TrackAdapter newFiles  💥
}