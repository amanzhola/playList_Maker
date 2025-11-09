// data/translator/WikiTitleResolver.kt
package com.example.playlistmaker.data.translator

import android.util.Log
import com.example.playlistmaker.data.network.wiki.WikiLangApi

class WikiTitleResolver(
    private val api: WikiLangApi
) {
    companion object { private const val TAG = "WikiResolver" }

    suspend fun ruToEnOrSelf(ru: String): String {
        return try {
            val resp = api.ruToEnTitle(ru)
            val en = resp.query
                ?.pages?.firstOrNull()
                ?.langlinks?.firstOrNull { it.lang == "en" }
                ?.title

            Log.d(TAG, "ru='$ru' → en='${en ?: ru}' (via Wikipedia)")
            en ?: ru
        } catch (t: Throwable) {
            Log.e(TAG, "wiki error: ${t.message}", t)
            ru
        }
    }
}
