package com.example.playlistmaker.presentation.utils

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.ConnectionSpec
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONArray
import org.json.JSONObject
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

/**
 * Старомобильный резолвер, повторяющий «новую» логику поиска:
 * 1) Поисковый запрос в YouTube (title + artist) → парсим ytInitialData → собираем кандидатов (id,title,channel,duration).
 * 2) Считаем скор как в NewPipe-варианте (match по title/artist, preferChannel, штраф Live/Cover/Remix).
 * 3) Берём лучший и резолвим ТОЛЬКО аудио (itag=140, AAC/M4A) через Invidious /latest_version с &local=true.
 *    Без HEAD — tiny Range GET (bytes=0-1), circuit-breaker по инстансам, кэш query→videoId и id→URL.
 * 4) Фоллбэк: Invidious без local; затем Piped ?local=true (если даёт JSON).
 *
 * Заточено под старые девайсы: минимальные редиректы/TLS, только audio/mp4 (m4a), без прогрессивного видео.
 */
object YoutubeDirectResolver {

    private const val TAG = "YoutubeLegacyResolver"

    // ─────────────── helpers: safe JSON access ───────────────
    private fun JSONObject.optStringOrNull(name: String): String? =
        try {
            if (isNull(name)) null else getString(name)
        } catch (_: Throwable) { null }?.takeIf { it.isNotBlank() && it != "null" }

    private fun JSONObject.optLongOrNull(name: String): Long? =
        try {
            if (isNull(name)) null else getLong(name)
        } catch (_: Throwable) { null }

    private fun JSONArray.optJSONObjectOrNull(index: Int): JSONObject? =
        try { if (index in 0 until length()) optJSONObject(index) else null } catch (_: Throwable) { null }

    // ─────────────── Результат ───────────────
    data class Result(
        val title: String?,
        val channel: String?,
        val durationSec: Long?,
        val audioUrl: String,
        val audioMime: String?,
        val videoPageUrl: String,
        val thumbUrl: String?
    )

    // ─────────────── Кандидаты из поиска ───────────────
    private data class Candidate(
        val id: String,
        val title: String?,
        val channel: String?,
        val durationSec: Long?,   // может быть null, если в выдаче нет
        val thumbUrl: String?
    )

    // ─────────────── Кэши ───────────────
    private data class CacheEntry(
        val url: String,
        val mime: String?,
        val cachedAtMs: Long,
        val expireEpoch: Long?
    )

    private val VID_CACHE = ConcurrentHashMap<String, Pair<String, Long>>()      // queryNorm -> (videoId, ts)
    private val DIRECT_CACHE = ConcurrentHashMap<String, CacheEntry>()           // "q:..." / "id:..." -> entry
    private fun nowSec() = System.currentTimeMillis() / 1000

    private fun parseExpireEpochFromUrl(u: String): Long? {
        val i = u.indexOf("expire="); if (i < 0) return null
        val tail = u.substring(i + 7)
        val end = tail.indexOf('&').let { if (it >= 0) tail.substring(0, it) else tail }
        return end.toLongOrNull()
    }
    private fun isCacheValid(e: CacheEntry): Boolean {
        e.expireEpoch?.let { return nowSec() < it - 30 }
        return System.currentTimeMillis() - e.cachedAtMs < 5 * 60_000
    }
    private fun getCache(key: String): Pair<String, String?>? {
        val e = DIRECT_CACHE[key] ?: return null
        val exp = e.expireEpoch ?: parseExpireEpochFromUrl(e.url)
        val valid = if (exp != null) nowSec() < exp - 30 else isCacheValid(e)
        if (!valid) return null
        // вместо HEAD — быстрый Range GET
        return if (httpRangeOk(e.url)) e.url to e.mime else null
    }
    private fun putCache(key: String, url: String, mime: String?) {
        DIRECT_CACHE[key] = CacheEntry(url, mime, System.currentTimeMillis(), parseExpireEpochFromUrl(url))
    }
    private fun putVid(queryNorm: String, id: String) { VID_CACHE[queryNorm] = id to System.currentTimeMillis() }
    private fun getVid(queryNorm: String): String? {
        val p = VID_CACHE[queryNorm] ?: return null
        if (System.currentTimeMillis() - p.second > 60 * 60_000) return null
        return p.first
    }

    // ─────────────── Circuit breaker ───────────────
    private object Ban {
        private val fails = ConcurrentHashMap<String, Int>()
        private val bannedUntil = ConcurrentHashMap<String, Long>()
        fun isBanned(base: String): Boolean = (bannedUntil[base] ?: 0L) > System.currentTimeMillis()
        fun fail(base: String) {
            val n = (fails[base] ?: 0) + 1
            fails[base] = n
            if (n >= 3) { bannedUntil[base] = System.currentTimeMillis() + 20 * 60_000; fails[base] = 0 }
        }
        fun ok(base: String) { fails[base] = 0; bannedUntil.remove(base) }
    }

    // ─────────────── HTTP ───────────────
    private val httpClient: OkHttpClient by lazy {
        val logger = HttpLoggingInterceptor { m -> Log.v("$TAG/HTTP", m) }.apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }
        val specs = listOf(
            ConnectionSpec.MODERN_TLS,
            ConnectionSpec.COMPATIBLE_TLS,
            ConnectionSpec.CLEARTEXT
        )
        OkHttpClient.Builder()
            .connectionSpecs(specs)
            .followRedirects(true)
            .followSslRedirects(true)
            .connectTimeout(12, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .writeTimeout(20, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .addInterceptor { chain ->
                val req = chain.request().newBuilder()
                    .header(
                        "User-Agent",
                        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) " +
                                "AppleWebKit/537.36 (KHTML, like Gecko) " +
                                "Chrome/124.0.0.0 Safari/537.36"
                    )
                    .header("Accept-Language", "en-US,en;q=0.8,ru;q=0.6")
                    .build()
                chain.proceed(req)
            }
            .addInterceptor(logger)
            .build()
    }

    private fun httpGetDetailed(url: String, acceptJson: Boolean = false): Triple<String?, String?, Int> {
        return try {
            val rb = Request.Builder().url(url)
            if (acceptJson) rb.header("Accept", "application/json")
            httpClient.newCall(rb.build()).execute().use { resp ->
                val code = resp.code
                val ctype = resp.body?.contentType()?.toString()
                val body = resp.body?.string()
                Triple(if (resp.isSuccessful) body else null, ctype, code)
            }
        } catch (t: Throwable) {
            Log.v("$TAG/HTTP", "<-- HTTP FAILED: ${t.javaClass.simpleName}: ${t.message}")
            Triple(null, null, -1)
        }
    }
    private fun httpGet(url: String): String? = httpGetDetailed(url).first

    // Range вместо HEAD
    private fun httpRangeOk(url: String): Boolean = try {
        val req = Request.Builder().url(url).get().header("Range", "bytes=0-0").build()
        httpClient.newCall(req).execute().use { resp ->
            resp.isSuccessful || resp.code == 206 || (resp.code in 300..399)
        }
    } catch (_: Throwable) { false }

    // tiny Range GET → (ok, finalUrl, contentType)
    private fun tinyGet(url: String): Triple<Boolean, String?, String?> = try {
        val req = Request.Builder().url(url).get().header("Range", "bytes=0-1").build()
        httpClient.newCall(req).execute().use { resp ->
            Triple(resp.code in 200..206, resp.request.url.toString(), resp.body?.contentType()?.toString())
        }
    } catch (_: Throwable) { Triple(false, null, null) }

    // ─────────────── Публичный API ───────────────
    suspend fun searchBestAudio(
        trackName: String,
        artistName: String?,
        preferChannel: String? = null,
        maxResults: Int = 5,           // возьмём побольше кандидатов для скоринга
        maxDurationSec: Int? = null,
        audioOnly: Boolean = true      // для старых — всегда true
    ): Result? = withContext(Dispatchers.IO) {
        val baseQuery = listOfNotNull(trackName, artistName).joinToString(" ").trim()
        val qNorm = baseQuery.lowercase(Locale.getDefault())
        val cacheKey = "q:$qNorm"

        Log.d(TAG, "🔍 track='$trackName', artist='$artistName', prefer='$preferChannel'")
        Log.d(TAG, "🔎 query: \"$baseQuery\" (audioOnly=$audioOnly)")

        // 0) кэш прямого URL
        getCache(cacheKey)?.let { (u, m) ->
            Log.d(TAG, "💾 cache hit (q) → Range ok")
            return@withContext Result(trackName, artistName, null, u, m, "", null)
        }

        // 0.1) кэш videoId
        getVid(qNorm)?.let { vid ->
            resolveByVideoId(vid, trackName, artistName)?.also { r ->
                putCache(cacheKey, r.audioUrl, r.audioMime)
                return@withContext r
            }
        }

        // 1) Поиск YouTube: несколько запросов, как в «новых»
        val enrichedQueries = listOf(
            "$baseQuery official audio",
            "$baseQuery topic",
            baseQuery
        )
        var best: Candidate? = null
        for (q in enrichedQueries) {
            val html = fetchSearchHtml(q)
            if (html.isBlank()) continue
            val cands = extractCandidatesFromHtml(html).take(maxResults.coerceAtLeast(1))
            if (cands.isEmpty()) continue
            val pick = cands.maxByOrNull { c ->
                score(
                    fullTitle = c.title ?: "",
                    wantTitle = trackName,
                    wantArtist = artistName,
                    channel = c.channel ?: "",
                    preferChannel = preferChannel
                )
            }
            if (pick != null) {
                best = pick
                break
            }
        }

        val videoId = best?.id ?: run {
            Log.e(TAG, "❌ no candidate found")
            return@withContext null
        }
        Log.d(TAG, "✅ chosen videoId=$videoId; title='${best.title}', by='${best.channel}', dur=${best.durationSec}")

        putVid(qNorm, videoId)

        resolveByVideoId(videoId, best.title ?: trackName, best.channel ?: artistName)?.also { r ->
            putCache(cacheKey, r.audioUrl, r.audioMime)
            return@withContext r
        }

        Log.e(TAG, "❌ nothing resolved for $videoId")
        null
    }

    // ─────────────── Соринг (как в NewPipe-варианте) ───────────────
    private fun norm(s: String) = s.lowercase(Locale.getDefault())
        .replace(Regex("[^\\p{L}\\p{Nd}\\s]+"), " ")
        .replace(Regex("\\s+"), " ")
        .trim()

    private fun score(
        fullTitle: String,
        wantTitle: String,
        wantArtist: String?,
        channel: String,
        preferChannel: String?
    ): Int {
        val ft = norm(fullTitle)
        val t  = norm(wantTitle)
        val a  = norm(wantArtist ?: "")
        var s  = 0
        if (t.isNotEmpty() && ft.contains(t)) s += 50
        if (a.isNotEmpty() && ft.contains(a)) s += 20
        if (!preferChannel.isNullOrBlank() && norm(preferChannel) == norm(channel)) s += 25
        if (!t.contains("live")  && ft.contains("live"))  s -= 10
        if (!t.contains("cover") && ft.contains("cover")) s -= 8
        if (!t.contains("remix") && ft.contains("remix")) s -= 6
        return s
    }

    // ─────────────── Резолв по videoId (Invidious → Piped) ───────────────
    private fun resolveByVideoId(videoId: String, title: String?, channel: String?): Result? {
        // id-кэш
        getCache("id:$videoId")?.let { (u, m) ->
            Log.d(TAG, "💾 cache hit (id) → Range ok")
            return Result(title, channel, null, u, m, "https://www.youtube.com/watch?v=$videoId", null)
        }

        // 1) Invidious (local=true)
        fetchViaInvidious(videoId, local = true)?.let { (u, m) ->
            putCache("id:$videoId", u, m)
            return Result(title, channel, null, u, m, "https://www.youtube.com/watch?v=$videoId", null)
        }

        // 2) Invidious (обычный, googlevideo)
        fetchViaInvidious(videoId, local = false)?.let { (u, m) ->
            putCache("id:$videoId", u, m)
            return Result(title, channel, null, u, m, "https://www.youtube.com/watch?v=$videoId", null)
        }

        // 3) Piped (последний шанс)
        fetchFromPiped(videoId)?.let { p ->
            val au = p.audioUrl ?: return@let null
            putCache("id:$videoId", au, p.audioMime)
            return Result(title ?: p.title, channel ?: p.uploader, p.durationSec, au, p.audioMime,
                "https://www.youtube.com/watch?v=$videoId", p.thumbnail)
        }

        return null
    }

    // ─────────────── Поиск (YouTube HTML) ───────────────
    private fun fetchSearchHtml(query: String): String {
        val q = query.replace("\\s+".toRegex(), "+")
        val mUrl   = "https://m.youtube.com/results?sp=EgIQAQ%3D%3D&hl=en&gl=US&persist_hl=1&persist_gl=1&search_query=$q"
        val wwwUrl = "https://www.youtube.com/results?search_query=$q"
        val mMirror   = "https://r.jina.ai/http://m.youtube.com/results?sp=EgIQAQ%3D%3D&hl=en&gl=US&persist_hl=1&persist_gl=1&search_query=$q"
        val wwwMirror = "https://r.jina.ai/http://www.youtube.com/results?search_query=$q"

        httpGet(mUrl)?.let { if (it.contains("ytInitialData")) return it }
        httpGet(wwwUrl)?.let { if (it.contains("ytInitialData")) return it }
        httpGet(mMirror)?.let { return it }
        httpGet(wwwMirror)?.let { return it }
        return ""
    }

    private fun extractCandidatesFromHtml(html: String): List<Candidate> {
        val json = extractObjectAssignedTo(html, "ytInitialData") ?: return emptyList()
        val out = ArrayList<Candidate>(10)
        try {
            val root = JSONObject(json)
            val contents = root.optJSONObject("contents")
            val twoCol = contents?.optJSONObject("twoColumnSearchResultsRenderer")
            val primary = twoCol?.optJSONObject("primaryContents")
            val sectionList = primary?.optJSONObject("sectionListRenderer")
            val sections = sectionList?.optJSONArray("contents") ?: JSONArray()

            for (s in 0 until sections.length()) {
                val sectionObj = sections.optJSONObjectOrNull(s) ?: continue
                val itemSection = sectionObj.optJSONObject("itemSectionRenderer") ?: continue
                val items = itemSection.optJSONArray("contents") ?: continue

                for (i in 0 until items.length()) {
                    val item = items.optJSONObjectOrNull(i) ?: continue
                    val video = item.optJSONObject("videoRenderer")
                        ?: item.optJSONObject("gridVideoRenderer")
                        ?: item.optJSONObject("compactVideoRenderer")
                        ?: continue

                    val id = video.optStringOrNull("videoId") ?: continue

                    val titleObj = video.optJSONObject("title")
                    val simple = titleObj?.optStringOrNull("simpleText")
                    val runs = titleObj?.optJSONArray("runs")
                    val title = simple ?: runs?.optJSONObjectOrNull(0)?.optStringOrNull("text")

                    val ownerText = video.optJSONObject("ownerText")
                    val chan = ownerText?.optJSONArray("runs")?.optJSONObjectOrNull(0)?.optStringOrNull("text")

                    // длительность "3:56" или "1:02:03"
                    val lengthText = video.optJSONObject("lengthText")
                    val durStr =
                        lengthText?.optStringOrNull("simpleText")
                            ?: video.optJSONArray("thumbnailOverlays")
                                ?.let { arr ->
                                    (0 until arr.length())
                                        .asSequence()
                                        .mapNotNull { arr.optJSONObjectOrNull(it) }
                                        .mapNotNull { it.optJSONObject("thumbnailOverlayTimeStatusRenderer") }
                                        .mapNotNull { it.optJSONObject("text")?.optStringOrNull("simpleText") }
                                        .firstOrNull()
                                }

                    val durationSec = parseDuration(durStr)

                    val thumbsArr = video.optJSONObject("thumbnail")?.optJSONArray("thumbnails")
                    val thumb = thumbsArr?.optJSONObjectOrNull(thumbsArr.length() - 1)?.optStringOrNull("url")

                    out.add(Candidate(id, title, chan, durationSec, thumb))
                }
            }
        } catch (t: Throwable) {
            Log.e(TAG, "ytInitialData parse error: ${t.message}")
        }
        // fallback по href, если парсер не сработал
        if (out.isEmpty()) {
            val re = Regex("""\/watch\?v=([A-Za-z0-9_-]{11})""")
            re.findAll(html).forEach { m ->
                m.groupValues.getOrNull(1)?.let { id ->
                    out.add(Candidate(id, null, null, null, null))
                }
            }
        }
        return out
    }

    private fun extractObjectAssignedTo(html: String, varName: String): String? {
        val patterns = listOf(
            Regex("""\bvar\s+$varName\s*=\s*\{"""),
            Regex("""window\["$varName"]\s*=\s*\{"""),
            Regex("""window\.$varName\s*=\s*\{"""),
            Regex("""\b$varName\s*=\s*\{""")
        )
        for (p in patterns) {
            val m = p.find(html) ?: continue
            val start = m.range.last + 1
            return consumeBalancedBraces(html, start - 1)
        }
        return null
    }

    private fun consumeBalancedBraces(src: String, startIndex: Int): String? {
        var i = startIndex
        if (i !in src.indices || src[i] != '{') return null
        var depth = 0; var inStr = false; var esc = false
        while (i < src.length) {
            val ch = src[i]
            if (inStr) {
                if (esc) esc = false
                else if (ch == '\\') esc = true
                else if (ch == '"') inStr = false
            } else {
                when (ch) {
                    '"' -> inStr = true
                    '{' -> depth++
                    '}' -> { depth--; if (depth == 0) return src.substring(startIndex, i + 1) }
                }
            }
            i++
        }
        return null
    }

    private fun parseDuration(s: String?): Long? {
        if (s.isNullOrBlank()) return null
        // "3:56" / "1:02:03"
        val parts = s.trim().split(":").mapNotNull { it.toLongOrNull() }
        if (parts.isEmpty()) return null
        return when (parts.size) {
            1 -> parts[0]
            2 -> parts[0] * 60 + parts[1]
            else -> parts[0] * 3600 + parts[1] * 60 + parts[2]
        }
    }

    // ─────────────── Invidious (только itag=140) ───────────────
    private fun fetchViaInvidious(videoId: String, local: Boolean): Pair<String, String?>? {
        val bases = listOf(
            "https://inv.nadeko.net",
            "https://yewtu.be",
            "https://invidious.privacydev.net",
            "https://invidious.flokinet.to",
            "https://yt.artemislena.eu",
            "https://invidious.protokolla.fi"
        )
        val extra = if (local) "&local=true" else ""
        for (base in bases) {
            if (Ban.isBanned(base)) continue
            val url = "$base/latest_version?id=$videoId&itag=140$extra"
            val (ok, finalUrl, _) = tinyGet(url)
            if (!ok || finalUrl.isNullOrBlank()) {
                Ban.fail(base); continue
            }
            Ban.ok(base)
            return finalUrl to "audio/mp4"
        }
        return null
    }

    // ─────────────── Piped (фоллбэк) ───────────────
    private data class PipedPick(
        val title: String?, val uploader: String?, val durationSec: Long?,
        val thumbnail: String?, val audioUrl: String?, val audioMime: String?
    )

    private fun fetchFromPiped(videoId: String): PipedPick? {
        val bases = listOf(
            "https://piped.video",
            "https://piped.lunar.icu",
            "https://piped.projectsegfau.lt"
        )
        var lastErr: String? = null
        for (base in bases) {
            if (Ban.isBanned(base)) continue
            val url = "$base/api/v1/streams/$videoId?local=true"
            val (body, ctype, code) = httpGetDetailed(url, acceptJson = true)
            val looksJson = body != null && (ctype?.contains("json", true) == true || body!!.firstOrNull() in listOf('{','['))
            if (!looksJson) { lastErr = "HTTP $code / not JSON"; Ban.fail(base); continue }

            try {
                val obj = JSONObject(body!!)
                val title = obj.optStringOrNull("title")
                val uploader = obj.optStringOrNull("uploader")
                val durationSec = obj.optLongOrNull("duration")
                val thumbnail = obj.optStringOrNull("thumbnailUrl")

                val audioArr = obj.optJSONArray("audioStreams") ?: JSONArray()
                var audioUrl: String? = null
                var audioMime: String? = null
                for (i in 0 until audioArr.length()) {
                    val a = audioArr.optJSONObjectOrNull(i) ?: continue
                    val urlA = a.optStringOrNull("url") ?: continue
                    val mt = a.optStringOrNull("mimeType")?.lowercase(Locale.getDefault()) ?: ""
                    val cont = a.optStringOrNull("container")?.lowercase(Locale.getDefault()) ?: ""
                    if (mt.startsWith("audio/mp4") || cont.contains("m4a") || mt.contains("mp4a")) {
                        audioUrl = urlA
                        audioMime = mt.ifBlank { "audio/mp4" }
                        break
                    }
                }
                if (audioUrl == null && audioArr.length() > 0) {
                    val a = audioArr.optJSONObjectOrNull(0)
                    audioUrl = a?.optStringOrNull("url")
                    audioMime = a?.optStringOrNull("mimeType")
                }
                if (audioUrl != null) {
                    Ban.ok(base)
                    return PipedPick(title, uploader, durationSec, thumbnail, audioUrl, audioMime)
                }
            } catch (t: Throwable) {
                lastErr = t.message; Ban.fail(base)
            }
        }
        Log.e(TAG, "Piped fallback failed: ${lastErr ?: "unknown"}")
        return null
    }
}
