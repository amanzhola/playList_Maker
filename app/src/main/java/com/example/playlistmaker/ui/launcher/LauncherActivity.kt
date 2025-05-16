package com.example.playlistmaker.ui.launcher

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.playlistmaker.ui.launcherPosters.TrackDetailActivity
import com.example.playlistmaker.domain.models.Track
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
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
                        showAlertDialog("Ошибка", "Получен пустой текст.")
                    }
                }
            }

            Intent.ACTION_VIEW -> {
                val uri: Uri? = intent.data
                if (uri != null) {
                    handleTrackFromUri(uri)
                } else {
                    showAlertDialog("Ошибка", "Некорректный Uri.")
                }
            }

            else -> finish()
        }
    }

    private fun handleTrackFromText(sharedText: String) {
        try {
            val trackArray = Gson().fromJson(sharedText, Array<Track>::class.java)
            if (trackArray.isNotEmpty()) {
                openTrackDetail(trackArray, 0)
            } else {
                showAlertDialog("Ошибка", "Массив треков пуст.")
            }
        } catch (e: JsonSyntaxException) {
            showAlertDialog("Ошибка", "Не удалось прочитать трек. Проверьте формат данных.")
        } catch (e: Exception) {
            showAlertDialog("Ошибка", "Произошла ошибка при обработке текста.")
        }
    }

    private fun handleTrackFromUri(uri: Uri) {
        try {
            val inputStream = contentResolver.openInputStream(uri)
            val json = inputStream?.bufferedReader().use { it?.readText() }

            if (!json.isNullOrEmpty()) {
                val trackArray = Gson().fromJson(json, Array<Track>::class.java)
                if (trackArray.isNotEmpty()) {
                    openTrackDetail(trackArray, 0) // Передаем весь массив и индекс 0
                } else {
                    showAlertDialog("Ошибка", "Массив треков пуст.")
                }
            } else {
                showAlertDialog("Ошибка", "Получен пустой JSON-файл.")
            }
        } catch (e: FileNotFoundException) {
            showAlertDialog("Ошибка", "Файл не найден.")
        } catch (e: IOException) {
            showAlertDialog("Ошибка", "Ошибка чтения JSON-файла.")
        } catch (e: JsonSyntaxException) {
            showAlertDialog("Ошибка", "Не удалось прочитать трек. Проверьте формат данных.")
        } catch (e: Exception) {
            showAlertDialog("Ошибка", "Произошла ошибка при обработке Uri.")
        }
    }

    private fun showAlertDialog(title: String, message: String) {
        AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun openTrackDetail(tracks: Array<Track>, trackIndex: Int) {
        val intent = Intent(this, TrackDetailActivity::class.java).apply {
            putExtra("track_list_json", Gson().toJson(tracks))
            putExtra("track_index", trackIndex)
        }
        startActivity(intent)
        finish()
    }
}