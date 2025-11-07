package com.example.playlistmaker.data.repository.moviePersons

import android.util.Log
import com.example.playlistmaker.data.network.wiki.WikiApi
import com.example.playlistmaker.domain.api.moviePersons.NamesRepository
import com.example.playlistmaker.domain.models.moviePerson.Person
import com.example.playlistmaker.domain.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException

class NamesRepositoryImpl(
    private val apiRu: WikiApi,
    private val apiEn: WikiApi
) : NamesRepository {

    override fun searchNames(expression: String): Flow<Resource<List<Person>>> = flow {
        try {
            val q = expression.trim()
            val lang = detectLang(q)
            val api = if (lang == Lang.EN) apiEn else apiRu
            val host = if (lang == Lang.EN) "en" else "ru"

            Log.d("WIKI_TEST", "generator lang=${lang.name} query='$q'")

            val resp = api.searchWithThumbs(q)
            val pages = resp.query?.pages.orEmpty()

            val persons = pages.map { p ->
                // приоритет: thumbnail.source → иначе ссылка на страницу
                val photo = p.thumbnail?.source
                    ?: "https://$host.wikipedia.org/wiki?curid=${p.pageid}"

                val desc = when {
                    !p.description.isNullOrBlank() -> p.description
                    !p.extract.isNullOrBlank() -> p.extract
                    else -> "" // бывает пусто
                }

                Person(
                    id = p.pageid.toString(),
                    name = p.title,
                    description = desc,
                    photoUrl = photo
                )
            }

            emit(Resource.Success(persons))

        } catch (e: HttpException) {
            emit(Resource.Error("HTTP ${e.code()} ${e.message() ?: ""}".trim()))
        } catch (e: Exception) {
            emit(Resource.Error("Ошибка сети: ${e.localizedMessage}"))
        }
    }

    private enum class Lang { RU, EN }

    private fun detectLang(q: String): Lang {
        var hasLatin = false
        var hasCyril = false
        for (ch in q) {
            when {
                ch in 'A'..'Z' || ch in 'a'..'z' -> hasLatin = true
                ch in 'А'..'Я' || ch in 'а'..'я' || ch == 'ё' || ch == 'Ё' -> hasCyril = true
            }
            if (hasLatin && hasCyril) break
        }
        return when {
            hasLatin && !hasCyril -> Lang.EN
            hasCyril && !hasLatin -> Lang.RU
            else -> Lang.RU // дефолт (можешь поменять)
        }
    }
}
