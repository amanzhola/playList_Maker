package com.example.playlistmaker.data.repository.movie

import android.util.Log
import com.example.playlistmaker.data.dto.movie.MovieAdvancedSearchDto
import com.example.playlistmaker.data.dto.movie.MovieSearchDto
import com.example.playlistmaker.data.movie_db.MovieDbConvertor
import com.example.playlistmaker.data.movie_db.MoviesDatabase
import com.example.playlistmaker.data.network.movie.IMDbApi
import com.example.playlistmaker.data.translator.TranslateBatcher
import com.example.playlistmaker.data.translator.WikiTitleResolver
import com.example.playlistmaker.domain.api.movie.MoviesRepository
import com.example.playlistmaker.domain.models.movie.Movie
import com.example.playlistmaker.domain.util.Resource
import com.example.playlistmaker.domain.util.isRussian
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class MoviesRepositoryImpl(
    private val apiService: IMDbApi,
    private val apiKey: String,
    private val batcher: TranslateBatcher, // 👈 добавили
    private val appDatabase: MoviesDatabase,        // 👈 добавили БД
    private val movieDbConvertor: MovieDbConvertor,  // 👈 добавили конвертер
    private val wikiResolver: WikiTitleResolver,
) : MoviesRepository {

    companion object { private const val TAG = "HistoryRepo" }

    override fun searchMovies(expression: String): Flow<Resource<List<Movie>>> = flow {
        try {

            // 1) RU → EN для запроса
            val isRuQuery = isRussian(expression)
            val q = if (isRuQuery) wikiResolver.ruToEnOrSelf(expression) else expression
            Log.d(TAG, "wiki q='$q' (from '$expression', isRu=$isRuQuery)")

            val (searchResponse, advancedSearchResponse) = coroutineScope {
                val searchDeferred = async { apiService.searchMovies(apiKey, q) }
                val advancedDeferred = async { apiService.getAdvancedSearch(apiKey, q) }

                Pair(searchDeferred.await(), advancedDeferred.await())
            }

            val searchResults = if (searchResponse.isSuccessful) searchResponse.body()?.results else null
            val advancedSearchResults = if (advancedSearchResponse.isSuccessful) advancedSearchResponse.body()?.results else null

            searchResponse.body()?.errorMessage?.takeIf { it.isNotEmpty() }?.let {
                emit(Resource.Error("Ошибка API (Search): $it"))
                return@flow
            }

            advancedSearchResponse.body()?.errorMessage?.takeIf { it.isNotEmpty() }?.let {
                emit(Resource.Error("Ошибка API (AdvancedSearch): $it"))
                return@flow
            }

            if (!searchResponse.isSuccessful && !advancedSearchResponse.isSuccessful) {
                emit(Resource.Error("Ошибка HTTP запросов: ${searchResponse.code()} / ${advancedSearchResponse.code()}"))
                return@flow
            }

            if (!searchResponse.isSuccessful) {
                emit(Resource.Error("Ошибка HTTP запроса Search: ${searchResponse.code()}"))
            }

            if (!advancedSearchResponse.isSuccessful) {
                emit(Resource.Error("Ошибка HTTP запроса AdvancedSearch: ${advancedSearchResponse.code()}"))
            }

            val searchDataMap = searchResults?.associateBy { it.id } ?: emptyMap()
            val advancedSearchDataMap = advancedSearchResults?.associateBy { it.id } ?: emptyMap()
            val primaryList = advancedSearchResults?.takeIf { it.isNotEmpty() }?: searchResults

            val combinedMovies = primaryList?.mapNotNull { primaryDto ->
                val id = when (primaryDto) {
                    is MovieSearchDto -> primaryDto.id
                    is MovieAdvancedSearchDto -> primaryDto.id
                    else -> return@mapNotNull null
                }

                val searchMovie = searchDataMap[id]
                val advancedMovie = advancedSearchDataMap[id]

                if (searchMovie == null && advancedMovie == null) return@mapNotNull null

                Movie(
                    id = id,
                    resultType = searchMovie?.resultType,   // 👈 добавили resultType
                    image = advancedMovie?.image ?: "",
                    title = advancedMovie?.title ?: "",
                    description = searchMovie?.description.takeIf { !it.isNullOrEmpty() }
                        ?: advancedMovie?.description,  // ✨ ⭐ 👤
                    runtimeStr = advancedMovie?.runtimeStr,
                    genres = advancedMovie?.genres,
                    plot = advancedMovie?.plot,
                    imDbRating = advancedMovie?.imDbRating,
                    year = advancedMovie?.description, // Возможно, тут лучше advancedMovie?.year  // 📅
                    inFavorite = false
                )
            } ?: emptyList()

            val uiList = if (isRuQuery) {
                // Пакетный быстрый перевод:
                batcher.moviesToRu(combinedMovies)
            } else combinedMovies

            if (combinedMovies.isEmpty()) {
                emit(Resource.Error("Ничего не найдено"))
            } else {
                // Сохраняем список фильмов в историю поиска (БД)

                // translate to Russian
                saveMovies(expression, uiList)
                emit(Resource.Success(uiList))
            }

        } catch (e: Exception) {
            emit(Resource.Error("Ошибка при выполнении запросов: ${e.localizedMessage ?: "Неизвестная ошибка"}"))
        }

    }.flowOn(Dispatchers.IO)

    // Сохраняем в базу данных
    private suspend fun saveMovies(expression: String, movies: List<Movie>) {
        val entities = movies.map(movieDbConvertor::map)
//        Log.d(TAG, "saveMovies(): expr='$expression', size=${entities.size}")
        appDatabase.movieDao().replaceAll(entities)
    }

    // пример хелпера, который переводит поля фильмов EN->RU пакетно
    suspend fun TranslateBatcher.moviesToRu(movies: List<Movie>): List<Movie> {
        val titles  = toRu(movies.map { it.title })
        val descs   = toRu(movies.map { it.description })
        val plots   = toRu(movies.map { it.plot })
        val genres  = toRu(movies.map { it.genres })

        return movies.mapIndexed { i, m ->
            m.copy(
                title       = titles[i] ?: m.title,
                description = descs[i],
                plot        = plots[i],
                genres      = genres[i]
            )
        }
    }
}
