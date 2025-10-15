package com.example.playlistmaker.ui.chat.badge

import android.annotation.SuppressLint
import android.content.Context
import org.json.JSONArray
import kotlin.math.min

object EmojiBadgePicker {

    // ️пул эмодзи
    private val POOL = listOf(
        "🍏","🎯","🙏","🧩","🐶","✨","☀️","🌤️","⛅️","🌧️","❄️","😅","✌️","🏁",
        "🧾","💃","🌼","🎵","💬","📝","📭","🤔"
    )

    private const val PREFS = "emoji_badge_prefs"
    private const val RECENT_LIMIT = 100  // не повторяем до 100 последних

    private fun prefs(ctx: Context) = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    private fun recentKey(chatId: String) = "recent_$chatId"
    private fun nonceKey(chatId: String, count: Int) = "nonce_${chatId}_$count"

    private fun loadRecent(ctx: Context, chatId: String): MutableList<String> {
        val raw = prefs(ctx).getString(recentKey(chatId), "[]") ?: "[]"
        val arr = JSONArray(raw)
        val out = mutableListOf<String>()
        for (i in 0 until arr.length()) out += arr.getString(i)
        return out
    }

    @SuppressLint("UseKtx")
    private fun saveRecent(ctx: Context, chatId: String, recent: MutableList<String>) {
        // обрежем до лимита
        while (recent.size > RECENT_LIMIT) recent.removeAt(0)
        val arr = JSONArray()
        recent.forEach { arr.put(it) }
        prefs(ctx).edit().putString(recentKey(chatId), arr.toString()).apply()
    }

    private fun loadNonce(ctx: Context, chatId: String, count: Int): Int =
        prefs(ctx).getInt(nonceKey(chatId, count), 0)

    @SuppressLint("UseKtx")
    private fun saveNonce(ctx: Context, chatId: String, count: Int, value: Int) {
        prefs(ctx).edit().putInt(nonceKey(chatId, count), value).apply()
    }

    // выбираем k эмодзи без повторов, но с заданным seed
    private fun pickK(pool: List<String>, k: Int, seed: Long): List<String> {
        if (k <= 0) return emptyList()
        val rnd = java.util.Random(seed)
        return pool.shuffled(rnd).take(min(k, pool.size))
    }

    /**
     * Вернёт «ядро» без финального 🔥 :
     * 1 → "" (только 🔥)
     * 2..5 → (count-1) эмодзи (без повторов), новая комбинация без коллизий с недавними
     * 6 → 4 эмодзи + ➕
     * 7+ → "😔➕➕"
     *
     * Гарантия «не повторять» обеспечивается `recent` + инкрементом nonce.
     * Для различия у разных пользователей — включаем myUid (и peerKey) в seed.
     */
    fun buildCore(
        context: Context,
        chatId: String,
        myUid: String,
        peerKey: String?, // для DM: peerUid; для групп: stableKey = sorted(otherUids).joinToString("#")
        count: Int
    ): String {
        if (count <= 1) return ""
        if (count >= 7) return "😔➕➕"

        val k = if (count in 2..5) count - 1 else 4 // для 6 возьмём 4 + «➕»

        val recent = loadRecent(context, chatId)
        var nonce = loadNonce(context, chatId, count)

        // до 50 попыток с разным nonce, чтобы найти неизведанную комбинацию
        repeat(50) {
            val seedStr = "$chatId|$myUid|${peerKey ?: ""}|$count|$nonce"
            val seed = seedStr.hashCode().toLong()
            val core = pickK(POOL, k, seed).joinToString("")
            if (core !in recent) {
                // сохраним
                recent += core
                saveRecent(context, chatId, recent)
                saveNonce(context, chatId, count, nonce + 1)
                // добавим «➕» на 6
                return if (count == 6) "$core➕" else core
            }
            nonce++
        }

        // fallback: если вдруг не нашли уникальную (маловероятно) — последняя
        val seedStr = "$chatId|$myUid|${peerKey ?: ""}|$count|$nonce"
        val seed = seedStr.hashCode().toLong()
        val core = pickK(POOL, k, seed).joinToString("")
        return if (count == 6) "$core➕" else core
    }
}
