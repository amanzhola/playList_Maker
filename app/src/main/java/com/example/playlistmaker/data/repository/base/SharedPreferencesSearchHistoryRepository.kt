package com.example.playlistmaker.data.repository.base

import android.annotation.SuppressLint
import android.content.SharedPreferences
import com.example.playlistmaker.data.song_db.FavoriteTrackDao
import com.example.playlistmaker.domain.models.search.Track
import com.example.playlistmaker.domain.repository.base.SearchHistoryRepository
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

// 📦 Репозиторий для хранения и управления историей поиска треков (add emoji)
// Использует SharedPreferences + Flow для наблюдения за изменениями
class SharedPreferencesSearchHistoryRepository(
    private val sharedPreferences: SharedPreferences,
    private val favoriteTrackDao: FavoriteTrackDao // ❤️ DAO для проверки избранного
) : SearchHistoryRepository {

    private val gson = Gson()
    private val historyFlow = MutableStateFlow<List<Track>>(emptyList())

    init {
        // 🧪 При инициализации — загружаем текущую историю
        CoroutineScope(Dispatchers.IO).launch {
            historyFlow.emit(getHistory())
        }
    }

    // 📡 Подписка на Flow истории
    override fun observeHistory(): Flow<List<Track>> = historyFlow

    // 🧾 Получаем историю (и сразу помечаем избранное)
    override suspend fun getHistory(): List<Track> {
        val json = sharedPreferences.getString(TRACK_HISTORY_LIST_KEY, null)
        val rawHistory = if (!json.isNullOrEmpty()) {
            gson.fromJson(json, Array<Track>::class.java).toList()
        } else emptyList()

        val favoriteIds = favoriteTrackDao.getFavoriteTrackIds()
        rawHistory.forEach { it.isFavorite = favoriteIds.contains(it.trackId) }

        return rawHistory
    }

    // ➕ Добавляем трек в начало истории
    override suspend fun addTrackToHistory(track: Track) {
        val current = getHistory().toMutableList()
        current.removeAll { it.trackId == track.trackId }

        if (current.size >= MAX_HISTORY_SIZE) {
            current.removeAt(current.lastIndex) // для API 29+
        }

        current.add(0, track)
        saveHistory(current)
    }

    // 💾 Сохраняем список в SharedPreferences и пушим в Flow
    @SuppressLint("UseKtx")
    override suspend fun saveHistory(tracks: List<Track>) {
        val json = gson.toJson(tracks)
        sharedPreferences.edit().putString(TRACK_HISTORY_LIST_KEY, json).apply()

        // 👇 немедленно пушим новое значение в Flow
        historyFlow.emit(tracks)  // 🔁 Обновляем Flow вручную
    }

    // 🗑️ Очищаем историю
    override suspend fun clearHistory() {
        saveHistory(emptyList())
    }

    companion object { // 🔑 Ключ для хранения истории в SharedPreferences
        private const val TRACK_HISTORY_LIST_KEY = "trackHistoryList"
        private const val MAX_HISTORY_SIZE = 10 // 🔢 Максимальный размер истории
    }
}
