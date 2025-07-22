package com.example.playlistmaker.data.network.movieDetails

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.example.playlistmaker.data.dto.movieCast.MovieCastRequest
import com.example.playlistmaker.data.dto.movieDetails.MovieDetailsRequest
import com.example.playlistmaker.data.dto.movieDetails.Response
import com.example.playlistmaker.data.dto.moviePersons.NamesSearchRequest

class RetrofitNetworkClient(
    private val imdbService: IMDbApiService,
    private val context: Context) : NetworkClient {

    override fun doRequest(dto: Any): Response {
        if (isConnected() == false) {
            return Response().apply { resultCode = -1 }
        }

//        if ((dto !is MoviesSearchRequest) && (dto !is MovieDetailsRequest)) {
//            return Response().apply { resultCode = 400 }
//        }

//        // Добавили ещё одну проверку
//        if ((dto !is MoviesSearchRequest) && (dto !is MovieDetailsRequest) && (dto !is MovieCastRequest)) {
//            return Response().apply { resultCode = 400 }
//        }

        // person
        if ((dto !is MoviesSearchRequest)
            && (dto !is MovieDetailsRequest)
            && (dto !is MovieCastRequest)
            && (dto !is NamesSearchRequest)) {
            return Response().apply { resultCode = 400 }
        }

//        val response = if (dto is MoviesSearchRequest) {
//            imdbService.searchMovies(dto.expression).execute()
//        } else {
//            imdbService.getMovieDetails((dto as MovieDetailsRequest).movieId).execute()
//        }
//        val body = response.body()
//        return if (body != null) {
//            body.apply { resultCode = response.code() }
//        } else {
//            Response().apply { resultCode = response.code() }
//        }

//        // Добавился ещё один if - тоже самое внизу через when
//        val response = if (dto is MoviesSearchRequest) {
//            imdbService.searchMovies(dto.expression).execute()
//        } else if (dto is MovieDetailsRequest) {
//            imdbService.getMovieDetails(dto.movieId).execute()
//        } else {
//            imdbService.getFullCast((dto as MovieCastRequest).movieId).execute()
//        }
//        val body = response.body()
//        return if (body != null) {
//            body.apply { resultCode = response.code() }
//        } else {
//            Response().apply { resultCode = response.code() }
//        }

//        // Добавили в выражение when ещё одну ветку
//        val response = when (dto) {
//            is MoviesSearchRequest -> imdbService.searchMovies(dto.expression).execute()
//            is MovieDetailsRequest -> imdbService.getMovieDetails(dto.movieId).execute()
//            else -> imdbService.getFullCast((dto as MovieCastRequest).movieId).execute()
//        }
//        val body = response.body()
//        return if (body != null) {
//            body.apply { resultCode = response.code() }
//        } else {
//            Response().apply { resultCode = response.code() }
//        }
        // + person
        val response = when (dto) {
            is NamesSearchRequest -> imdbService.searchNames(dto.expression).execute()
            is MoviesSearchRequest -> imdbService.searchMovies(dto.expression).execute()
            is MovieDetailsRequest -> imdbService.getMovieDetails(dto.movieId).execute()
            else -> imdbService.getFullCast((dto as MovieCastRequest).movieId).execute()
        }
        val body = response.body()
        return if (body != null) {
            body.apply { resultCode = response.code() }
        } else {
            Response().apply { resultCode = response.code() }
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