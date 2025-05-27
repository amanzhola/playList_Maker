package com.example.playlistmaker.data.repository.movie

import com.example.playlistmaker.data.dto.movie.MovieAdvancedSearchDto
import com.example.playlistmaker.data.dto.movie.MovieSearchDto
import com.example.playlistmaker.data.dto.SearchResponse
import com.example.playlistmaker.data.network.movie.IMDbApi
import com.example.playlistmaker.domain.api.movie.MoviesRepository
import com.example.playlistmaker.domain.models.movie.Movie
import com.example.playlistmaker.domain.util.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import retrofit2.Response


class MoviesRepositoryImpl(
    private val apiService: IMDbApi,
    private val apiKey: String
) : MoviesRepository {

    override fun searchMovies(expression: String): Flow<Resource<List<Movie>>> = flow {
        try {
            val searchResponseDeferred = withContext(Dispatchers.IO) {
                async { apiService.searchMovies(apiKey, expression) }
            }
            val advancedSearchResponseDeferred = withContext(Dispatchers.IO) {
                async { apiService.getAdvancedSearch(apiKey, expression) }
            }

            val searchResponse: Response<SearchResponse<MovieSearchDto>> = searchResponseDeferred.await()
            val advancedSearchResponse: Response<SearchResponse<MovieAdvancedSearchDto>> = advancedSearchResponseDeferred.await()

            val searchResults = if (searchResponse.isSuccessful) searchResponse.body()?.results else null
            val advancedSearchResults = if (advancedSearchResponse.isSuccessful) advancedSearchResponse.body()?.results else null

            if (searchResponse.body()?.errorMessage?.isNotEmpty() == true) {
                emit(Resource.Error("Ошибка API (Search): ${searchResponse.body()?.errorMessage}"))
                return@flow
            }
            if (advancedSearchResponse.body()?.errorMessage?.isNotEmpty() == true) {
                emit(Resource.Error("Ошибка API (AdvancedSearch): ${advancedSearchResponse.body()?.errorMessage}"))
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

            val combinedMovies = primaryList?.mapNotNull { primaryMovieDto ->
                val id = when (primaryMovieDto) {
                    is MovieSearchDto -> primaryMovieDto.id
                    is MovieAdvancedSearchDto -> primaryMovieDto.id
                    else -> return@mapNotNull null
                }

                val searchMovieDto = searchDataMap[id]
                val advancedSearchMovieDto = advancedSearchDataMap[id]

                if (searchMovieDto == null && advancedSearchMovieDto == null) {
                    return@mapNotNull null
                }

                Movie(
                    id = id,
                    image = advancedSearchMovieDto?.image ?: "",
                    title = advancedSearchMovieDto?.title ?: "",
                    description = if (!searchMovieDto?.description.isNullOrEmpty()) {
                        searchMovieDto?.description // ✨ ⭐ 👤
                    } else {
                        advancedSearchMovieDto?.description
                    },
                    runtimeStr = advancedSearchMovieDto?.runtimeStr,
                    genres = advancedSearchMovieDto?.genres,
                    plot = advancedSearchMovieDto?.plot,
                    imDbRating = advancedSearchMovieDto?.imDbRating,
                    year = advancedSearchMovieDto?.description, // 📅
                    inFavorite = false
                )
            } ?: emptyList()

            if (searchResults.isNullOrEmpty() && advancedSearchResults.isNullOrEmpty()) {
                emit(Resource.Error("Ничего не найдено"))
            } else {
                emit(Resource.Success(combinedMovies))
            }


        } catch (e: Exception) {
            emit(Resource.Error("Ошибка при выполнении запросов: ${e.localizedMessage}"))
        }
    }
}