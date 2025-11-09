package com.example.playlistmaker.data.translator

import com.example.playlistmaker.domain.util.TextTranslator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit

/**
 * Пакетный перевод с:
 *  - дедупликацией одинаковых строк
 *  - LRU-кэшем (in-memory)
 *  - ограниченным параллелизмом
 */
class TranslateBatcher(
    private val translator: TextTranslator,
    private val parallelism: Int = 4,
    private val cacheLimit: Int = 500
) {

    // Простейший LRU
    private val cache = object : LinkedHashMap<String, String>(cacheLimit, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, String>?) =
            size > cacheLimit
    }

    // Нормализация ключа: trim + схлопывание пробелов
    private fun norm(s: String) = s.trim().replace(Regex("\\s+"), " ")

    /** Пакетно EN->RU (null и пустые возвращаются как есть) */
    suspend fun toRu(list: List<String?>): List<String?> =
        batch(list) { translator.toRussian(it) }

    /** Пакетно RU->EN (null и пустые возвращаются как есть) */
    suspend fun toEn(list: List<String?>): List<String?> =
        batch(list) { translator.toEnglish(it) }

    private suspend fun batch(
        items: List<String?>,
        translate: suspend (String) -> String
    ): List<String?> = coroutineScope {
        // 1) Собираем индексы дубликатов по нормализованному ключу
        val keyToIdxs = LinkedHashMap<String, MutableList<Int>>()
        items.forEachIndexed { i, raw ->
            val k = raw?.let(::norm)
            if (!k.isNullOrEmpty()) {
                keyToIdxs.getOrPut(k) { mutableListOf() } += i
            }
        }

        // 2) Отберём те ключи, которых нет в кэше
        val toTranslateKeys = synchronized(cache) {
            keyToIdxs.keys.filter { !cache.containsKey(it) }
        }

        // 3) Переводим с ограниченным параллелизмом
        val gate = Semaphore(parallelism)
        toTranslateKeys.map { k ->
            async(Dispatchers.IO) {
                gate.withPermit {
                    val out = translate(k)
                    synchronized(cache) { cache[k] = out }
                }
            }
        }.awaitAll()

        // 4) Собираем результат в исходном порядке
        val out = items.toMutableList()
        keyToIdxs.forEach { (k, idxs) ->
            val v = synchronized(cache) { cache[k] } ?: k
            idxs.forEach { out[it] = v }
        }
        out
    }
}
