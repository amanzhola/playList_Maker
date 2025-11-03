package com.example.playlistmaker.presentation.utils

// NewPipeExtractor
// Downloader API (из NewPipeExtractor)
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.schabi.newpipe.extractor.NewPipe
import org.schabi.newpipe.extractor.ServiceList
import org.schabi.newpipe.extractor.exceptions.ExtractionException
import org.schabi.newpipe.extractor.linkhandler.SearchQueryHandler
import org.schabi.newpipe.extractor.search.SearchInfo
import org.schabi.newpipe.extractor.stream.AudioStream
import org.schabi.newpipe.extractor.stream.StreamInfo
import org.schabi.newpipe.extractor.stream.StreamInfoItem
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets
import java.util.Locale
import org.schabi.newpipe.extractor.downloader.Downloader as NPDownloader
import org.schabi.newpipe.extractor.downloader.Request as NPRequest
import org.schabi.newpipe.extractor.downloader.Response as NPResponse

object YoutubeNewPipeResolver {

    data class Result(
        val title: String,
        val channel: String,
        val durationSec: Long?,
        val audioUrl: String,
        val audioMime: String?,
        val videoPageUrl: String,
        val thumbUrl: String?,
        val progressiveVideoUrl: String?,
        val progressiveMime: String?
    )

    @Volatile private var inited = false

    private fun ensureInit() {
        if (inited) return
        NewPipe.init(object : NPDownloader() {
            override fun execute(request: NPRequest): NPResponse {
                val urlStr: String = request.url()
                val method: String = request.httpMethod()
                val headers: Map<String, List<String>>? = request.headers()
                val body: ByteArray? = request.dataToSend()

                val conn = (URL(urlStr).openConnection() as HttpURLConnection).apply {
                    requestMethod = method
                    connectTimeout = 15_000
                    readTimeout = 20_000
                    doInput = true
                    headers?.forEach { (k, vs) -> vs.forEach { v -> addRequestProperty(k, v) } }
                    if (body != null) {
                        doOutput = true
                        outputStream.use { os -> os.write(body) }
                    }
                }

                val code = conn.responseCode
                val msg  = conn.responseMessage ?: ""
                val bytes = try {
                    (if (code in 200..299) conn.inputStream else conn.errorStream)
                        ?.use { it.readBytes() } ?: ByteArray(0)
                } catch (_: Exception) { ByteArray(0) }

                val respHeaders: Map<String, List<String>> = (conn.headerFields ?: emptyMap())
                    .filterKeys { it != null }
                    .mapValues { it.value ?: emptyList() }

                val bodyStr: String = try { String(bytes, StandardCharsets.UTF_8) } catch (_: Exception) { "" }
                val latestUrl: String? = try { conn.url?.toString() } catch (_: Exception) { null }

                return NPResponse(code, msg, respHeaders, bodyStr, latestUrl)
            }
        })
        inited = true
    }

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

    private fun pickAudioForMediaPlayer(streams: List<AudioStream>): AudioStream? =
        // 1) m4a/aac — максимально совместимо
        streams.firstOrNull {
            val mt = it.format?.mimeType?.lowercase(Locale.getDefault()) ?: ""
            mt.contains("audio/mp4") || mt.contains("m4a") || mt.contains("mp4a")
        }
        // 2) по расширению в URL (null-safe)
            ?: streams.firstOrNull { s -> s.isUrl && s.content.contains(".m4a") }
            // 3) иначе — самый «жирный» поток
            ?: streams.maxByOrNull { it.bitrate }

    suspend fun searchBestAudio(
        trackName: String,
        artistName: String?,
        preferChannel: String? = null,
        maxResults: Int = 1,
        maxDurationSec: Int? = null
    ): Result? = withContext(Dispatchers.IO) {
        try {
            ensureInit()

            val query = buildString {
                append(trackName)
                if (!artistName.isNullOrBlank()) append(" ").append(artistName)
            }.trim()

            val service = ServiceList.YouTube

            // Нужен SearchQueryHandler (а не строка)
            val handler: SearchQueryHandler =
                service.searchQHFactory.fromQuery(query, emptyList(), null)

            // Результаты поиска
            val search: SearchInfo = SearchInfo.getInfo(service, handler)

            // В этой версии элементы в relatedItems (унаследовано от ListInfo)
            val rawItems = search.relatedItems ?: emptyList()

            val items: List<StreamInfoItem> = rawItems
                .filterIsInstance<StreamInfoItem>()
                .take(maxResults.coerceAtLeast(1))

            if (items.isEmpty()) return@withContext null

            val cand: StreamInfoItem = items.maxByOrNull { item ->
                score(
                    fullTitle = item.name ?: "",
                    wantTitle = trackName,
                    wantArtist = artistName,
                    channel = item.uploaderName ?: "",
                    preferChannel = preferChannel
                )
            } ?: return@withContext null

            val videoUrl: String = cand.url ?: return@withContext null

            // Подробная инфа по ролику
            val info: StreamInfo = StreamInfo.getInfo(service, videoUrl)

            // Длительность — из StreamInfoItem (сек.)
            val durSec: Long? = try { cand.duration } catch (_: Throwable) { null }?.toLong()

            // Лучший аудио-поток
            val audio: AudioStream = pickAudioForMediaPlayer(info.audioStreams ?: emptyList())
                ?: return@withContext null

            if (maxDurationSec != null && durSec != null && durSec > maxDurationSec) return@withContext null

            // Прогрессивный mp4 (видео+аудио)
            val progressive = info.videoStreams?.firstOrNull { vs ->
                val mt = vs.format?.mimeType?.lowercase(Locale.getDefault()) ?: ""
                mt.contains("mp4") || (vs.isUrl && vs.content.contains(".mp4"))
            }
            // Миниатюра: и у info, и у item — список thumbnails
            val thumb: String? =
                info.thumbnails.firstOrNull()?.url
                    ?: cand.thumbnails.firstOrNull()?.url

            Result(
                title = info.name.orEmpty(),
                channel = info.uploaderName.orEmpty(),
                durationSec = durSec,
                audioUrl = if (audio.isUrl) audio.content else return@withContext null,
                audioMime = audio.format?.mimeType,
                videoPageUrl = info.url,
                thumbUrl = thumb,
                progressiveVideoUrl = progressive?.let { if (it.isUrl) it.content else null },
                progressiveMime = progressive?.format?.mimeType
            )
        } catch (_: ExtractionException) {
            null
        } catch (_: Exception) {
            null
        }
    }
}