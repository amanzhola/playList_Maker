package com.example.playlistmaker.data.repository.movie

import android.util.Log
import com.example.playlistmaker.data.dto.movie.MovieAdvancedSearchDto
import com.example.playlistmaker.data.dto.movie.MovieSearchDto
import com.example.playlistmaker.data.network.movie.IMDbApi
import com.example.playlistmaker.domain.api.movie.MoviesRepository
import com.example.playlistmaker.domain.models.movie.Movie
import com.example.playlistmaker.domain.util.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class MoviesRepositoryImpl(
    private val apiService: IMDbApi,
    private val apiKey: String
) : MoviesRepository {

    init {
        Log.d("MoviesRepo", "MoviesRepositoryImpl created")
    }

    override fun searchMovies(expression: String): Flow<Resource<List<Movie>>> = flow {
        try {
            val (searchResponse, advancedSearchResponse) = coroutineScope {
                val searchDeferred = async { apiService.searchMovies(apiKey, expression) }
                val advancedDeferred = async { apiService.getAdvancedSearch(apiKey, expression) }

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

            val primaryList = advancedSearchResults ?: searchResults

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

            if (combinedMovies.isEmpty()) {
                emit(Resource.Error("Ничего не найдено"))
            } else {
                emit(Resource.Success(combinedMovies))
            }

        } catch (e: Exception) {
            emit(Resource.Error("Ошибка при выполнении запросов: ${e.localizedMessage ?: "Неизвестная ошибка"}"))
        }
    }.flowOn(Dispatchers.IO)
}
