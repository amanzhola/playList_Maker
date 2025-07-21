package com.example.playlistmaker.data.network.movieDetails

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.example.playlistmaker.data.dto.movieCast.MovieCastRequest
import com.example.playlistmaker.data.dto.movieDetails.MovieDetailsRequest
import com.example.playlistmaker.data.dto.movieDetails.Response
import com.example.playlistmaker.data.dto.moviePersons.NamesSearchRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RetrofitNetworkClient(
    private val imdbService: IMDbApiService,
    private val context: Context) : NetworkClient {

    override suspend fun doRequest(dto: Any): Response {
        if (isConnected() == false) {
            return Response().apply { resultCode = -1 }
        }

        // person
        if ((dto !is MoviesSearchRequest)
            && (dto !is MovieDetailsRequest)
            && (dto !is MovieCastRequest)
            && (dto !is NamesSearchRequest)) {
            return Response().apply { resultCode = 400 }
        }

    return withContext(Dispatchers.IO) {
        try {
            val response = when (dto) {
                is NamesSearchRequest -> imdbService.searchNames(dto.expression)
                is MoviesSearchRequest -> imdbService.searchMovies(dto.expression)
                is MovieDetailsRequest -> imdbService.getMovieDetails(dto.movieId)
                is MovieCastRequest -> imdbService.getFullCast(dto.movieId)
                else -> throw IllegalArgumentException("Unknown request type: ${dto::class.simpleName}")
            }
            response.apply { resultCode = 200 }
        } catch (e: Throwable) {
            Response().apply { resultCode = 500 }
        }
    }
}

    private fun isConnected(): Boolean {
        val connectivityManager = context.getSystemService(
            Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val capabilities = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
        if (capabilities != null) {
            when {
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> return true
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> return true
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> return true
            }
        }
        return false
    }
}