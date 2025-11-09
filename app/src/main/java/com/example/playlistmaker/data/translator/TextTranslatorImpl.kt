package com.example.playlistmaker.data.translator

import com.example.playlistmaker.domain.util.Lang
import com.example.playlistmaker.domain.util.TextTranslator
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.translate.Translator // <-- ВАЖНО правильный импорт
import kotlinx.coroutines.tasks.await

class TextTranslatorImpl(
    private val enToRu: Translator, // EN -> RU
    private val ruToEn: Translator  // RU -> EN
) : TextTranslator {

    private val ruCache = LinkedHashMap<String, String>(200, 0.75f, true)
    private val enCache = LinkedHashMap<String, String>(200, 0.75f, true)

    private suspend fun ensure(t: Translator) {
        val cond = DownloadConditions.Builder().build()
        t.downloadModelIfNeeded(cond).await()
    }

    private fun putLimited(map: LinkedHashMap<String, String>, k: String, v: String) {
        map[k] = v
        if (map.size > 300) map.remove(map.entries.first().key) // простейший LRU
    }

    override suspend fun detectLang(text: String): Lang {
        val hasRu = text.any { it in 'А'..'я' || it == 'Ё' || it == 'ё' }
        val hasEn = text.any { it in 'A'..'Z' || it in 'a'..'z' }
        return when {
            hasRu && !hasEn -> Lang.RU
            hasEn && !hasRu -> Lang.EN
            else -> Lang.OTHER
        }
    }

    override suspend fun toRussian(text: String): String {
        if (text.isBlank()) return text
        ruCache[text]?.let { return it }
        ensure(enToRu)
        val out = enToRu.translate(text).await()
        putLimited(ruCache, text, out)
        return out
    }

    override suspend fun toEnglish(text: String): String {
        if (text.isBlank()) return text
        enCache[text]?.let { return it }
        ensure(ruToEn)
        val out = ruToEn.translate(text).await()
        putLimited(enCache, text, out)
        return out
    }
}
