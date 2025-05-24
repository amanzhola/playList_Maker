package com.example.playlistmaker.creator

import com.example.playlistmaker.domain.api.movie.MoviesInteraction
import com.example.playlistmaker.data.repository.movie.MoviesRepositoryImpl
import android.app.Activity
import android.content.Context
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.playlistmaker.data.local.LocalStorage
import com.example.playlistmaker.data.network.weather.ForecaApi
import com.example.playlistmaker.data.network.movie.IMDbApi
import com.example.playlistmaker.data.network.search.ITunesApi
import com.example.playlistmaker.data.network.search.RetrofitAudioNetworkClient
import com.example.playlistmaker.data.network.base.RetrofitInstance
import com.example.playlistmaker.data.network.weather.RetrofitWeatherNetworkClient
import com.example.playlistmaker.data.repository.base.AgreementImpl
import com.example.playlistmaker.data.repository.search.AudioRepositoryImpl
import com.example.playlistmaker.data.repository.base.AudioSingleTrackImpl
import com.example.playlistmaker.data.repository.base.AudioTracksImpl
import com.example.playlistmaker.data.repository.base.FavoritesRepositoryImpl
import com.example.playlistmaker.data.repository.base.GsonMovieSerializer
import com.example.playlistmaker.data.repository.base.GsonTrackSerializer
import com.example.playlistmaker.data.repository.base.LanguageRepositoryImpl
import com.example.playlistmaker.data.repository.base.NavigationUseCaseImpl
import com.example.playlistmaker.data.repository.base.NetworkRepositoryImpl
import com.example.playlistmaker.data.repository.base.NetworkStatusCheckerImpl
import com.example.playlistmaker.data.repository.base.ResourceColorProviderImpl
import com.example.playlistmaker.data.repository.base.ShareImpl
import com.example.playlistmaker.data.repository.base.ShareMovieImpl
import com.example.playlistmaker.data.repository.base.SharedPreferencesSearchHistoryRepository
import com.example.playlistmaker.data.repository.base.SharedPrefsMovieStorage
import com.example.playlistmaker.data.repository.base.SharedPrefsTrackStorage
import com.example.playlistmaker.data.repository.base.SupportImpl
import com.example.playlistmaker.data.repository.base.ThemeRepositoryImpl
import com.example.playlistmaker.data.repository.base.TrackListIntentParserImpl
import com.example.playlistmaker.data.repository.weather.WeatherRepositoryImpl
import com.example.playlistmaker.domain.api.search.AudioInteraction
import com.example.playlistmaker.domain.api.search.AudioNetworkClient
import com.example.playlistmaker.domain.api.player.AudioPlayerInteraction
import com.example.playlistmaker.domain.api.search.AudioRepository
import com.example.playlistmaker.domain.api.base.LanguageInteraction
import com.example.playlistmaker.domain.api.movie.MovieSerializer
import com.example.playlistmaker.domain.api.movie.MovieStorageHelper
import com.example.playlistmaker.domain.api.movie.MoviesRepository
import com.example.playlistmaker.domain.api.base.NavigationUseCase
import com.example.playlistmaker.domain.api.base.NetworkStatusChecker
import com.example.playlistmaker.domain.api.base.SearchHistoryInteraction
import com.example.playlistmaker.domain.api.base.ThemeInteraction
import com.example.playlistmaker.domain.api.base.TrackSerializer
import com.example.playlistmaker.domain.api.base.TrackStorageHelper
import com.example.playlistmaker.domain.api.weather.WeatherInteraction
import com.example.playlistmaker.domain.api.weather.WeatherRepository
import com.example.playlistmaker.domain.api.weather.weatherNetworkClient
import com.example.playlistmaker.domain.impl.search.AudioInteractionImpl
import com.example.playlistmaker.domain.impl.player.AudioPlayerInteractionImpl
import com.example.playlistmaker.domain.impl.base.LanguageInteractionImpl
import com.example.playlistmaker.domain.impl.movie.MoviesInteractionImpl
import com.example.playlistmaker.domain.impl.base.SearchHistoryInteractionImpl
import com.example.playlistmaker.domain.impl.base.ThemeInteractionImpl
import com.example.playlistmaker.domain.impl.weather.WeatherInteractionImpl
import com.example.playlistmaker.domain.repository.base.Agreement
import com.example.playlistmaker.domain.repository.base.AudioSingleTrackShare
import com.example.playlistmaker.domain.repository.base.AudioTracksShare
import com.example.playlistmaker.domain.repository.base.FavoritesRepository
import com.example.playlistmaker.domain.repository.base.NetworkRepository
import com.example.playlistmaker.domain.repository.base.ResourceColorProvider
import com.example.playlistmaker.domain.repository.base.Share
import com.example.playlistmaker.domain.repository.base.ShareMovie
import com.example.playlistmaker.domain.repository.base.Support
import com.example.playlistmaker.domain.repository.base.TrackListIntentParser
import com.example.playlistmaker.domain.usecases.base.CheckInternetConnectionUseCase
import com.example.playlistmaker.domain.usecases.movie.ToggleFavoriteUseCase
import com.example.playlistmaker.presentation.searchPostersViewModels.ExtraOptionViewModelFactory
import com.example.playlistmaker.presentation.searchViewModels.SearchViewModelFactory
import com.example.playlistmaker.presentation.mainViewModels.MainViewModel
import com.example.playlistmaker.presentation.movieViewModels.MoviesViewModelFactory
import com.example.playlistmaker.presentation.utils.activityHelper.FailUiController
import com.google.gson.Gson
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object Creator {

    private lateinit var appContext: Context

    fun init(context: Context) {
        appContext = context.applicationContext
    }

    private fun getImdbService(): IMDbApi {
        val imdbBaseUrl = "https://tv-api.com"
        val retrofit = Retrofit.Builder()
            .baseUrl(imdbBaseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        return retrofit.create(IMDbApi::class.java)
    }

    private fun getMoviesRepository(): MoviesRepository {
        val apiKey = "k_zcuw1ytf"
        val imdbService = getImdbService()
        return MoviesRepositoryImpl(imdbService, apiKey)
    }

    fun provideMoviesInteraction(): MoviesInteraction {
        return MoviesInteractionImpl(getMoviesRepository())
    }

    // --- Методы для погоды ---

    private fun getWeatherService(): ForecaApi {
        val weatherBaseUrl = "https://fnw-us.foreca.com"
        val retrofit = Retrofit.Builder()
            .baseUrl(weatherBaseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        return retrofit.create(ForecaApi::class.java)
    }

    private fun getWeatherNetworkClient(): weatherNetworkClient {
        val weatherService = getWeatherService()
        val token = "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJodHRwOlwvXC9mbnctdXMuZm9yZWNhLmNvbVwvYXV0aG9yaXplXC90b2tlbiIsImlhdCI6MTc0NjExNTY3MCwiZXhwIjo5OTk5OTk5OTk5LCJuYmYiOjE3NDYxMTU2NzAsImp0aSI6IjdhODU1ODA2MjQ5Yjk1ODQiLCJzdWIiOiJhbG11c2hhX3llcyIsImZtdCI6IlhEY09oakM0MCtBTGpsWVR0amJPaUE9PSJ9.h8xnf_MIoK9F5f4rL922g5Ts8bVsYoSx0hKZgiPqCsg"
        return RetrofitWeatherNetworkClient(token, weatherService)
    }

    private fun getWeatherRepository(): WeatherRepository {
        val networkClient = getWeatherNetworkClient()
        return WeatherRepositoryImpl(networkClient)
    }

    fun provideWeatherInteraction(): WeatherInteraction {
        val repository = getWeatherRepository()
        return WeatherInteractionImpl(repository)
    }

    // --- Методы для аудио ---

    // --- Методы для iTunes API ---
    private fun getITunesService(): ITunesApi {
        val baseUrl = "https://itunes.apple.com"
        val retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        return retrofit.create(ITunesApi::class.java)
    }

    private fun getAudioNetworkClient(): AudioNetworkClient {
        return RetrofitAudioNetworkClient(getITunesService())
    }

    private fun getAudioRepository(): AudioRepository {
        return AudioRepositoryImpl(getAudioNetworkClient())
    }

    private fun getSearchHistoryInteraction(): SearchHistoryInteraction {
        val sharedPreferences = appContext.getSharedPreferences("search_history", Context.MODE_PRIVATE)
        val repository = SharedPreferencesSearchHistoryRepository(sharedPreferences)
        return SearchHistoryInteractionImpl(repository)
    }

    // --- Метод для предоставления AudioInteraction с учетом SearchHistoryRepository ---
    private fun provideAudioInteraction(): AudioInteraction {
        val searchHistoryInteraction = getSearchHistoryInteraction()
        val audioRepository = getAudioRepository()
        return AudioInteractionImpl(audioRepository, searchHistoryInteraction)
    }

    // --- Метод для предоставления AudioPlayer ---
    private var audioPlayerInstance: AudioPlayerInteraction? = null

    fun provideAudioPlayer(): AudioPlayerInteraction {
        if (audioPlayerInstance == null) {
            audioPlayerInstance = AudioPlayerInteractionImpl()
        }
        return audioPlayerInstance!!
    }

    fun provideSearchViewModelFactory(): SearchViewModelFactory {
        return SearchViewModelFactory(
            audioInteraction = provideAudioInteraction(),
            searchHistoryInteraction = getSearchHistoryInteraction()
        )
    }

    fun provideExtraOptionViewModelFactory(): ExtraOptionViewModelFactory {
        return ExtraOptionViewModelFactory(
            provideAudioPlayer()
        )
    }

    // --- Метод для предоставления SettingsActivity переключение темы ---
    fun provideThemeInteraction(): ThemeInteraction {
        return ThemeInteractionImpl(ThemeRepositoryImpl(appContext))
    }

    fun provideLanguageInteraction(): LanguageInteraction {
        return LanguageInteractionImpl(LanguageRepositoryImpl(appContext))
    }

    // --- Метод для предоставления MainActivity ---
    private fun provideNavigationUseCase(): NavigationUseCase {
        return NavigationUseCaseImpl(appContext)
    }

    @Suppress("UNCHECKED_CAST")
    fun provideMainViewModelFactory(): ViewModelProvider.Factory {
        val useCase = provideNavigationUseCase()
        val lang = provideLanguageInteraction()

        return object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return MainViewModel(useCase, lang) as T
            }
        }
    }

    fun provideTrackShareService(activity: Activity): AudioTracksShare {
        return AudioTracksImpl(activity)
    }

    fun provideShareHelper(activity: Activity): AudioSingleTrackShare {
        val trackShareService = provideTrackShareService(activity)
        return AudioSingleTrackImpl(activity, trackShareService)
    }

    fun provideShareMovieHelper(activity: Activity): ShareMovie {
        return ShareMovieImpl(activity)
    }

    fun provideShare(activity: Activity): Share {
        return ShareImpl(activity)
    }

    fun provideSupport(activity: Activity, mainLayout: ViewGroup, failTextView: TextView): Support {
        val failUiController = FailUiController(activity, mainLayout, failTextView)
        return SupportImpl(activity, failUiController)
    }

    fun provideAgreement(
        activity: Activity,
        mainLayout: ViewGroup,
        failTextView: TextView
    ): Agreement {
        val failUiController = FailUiController(activity, mainLayout, failTextView)
        val checkInternetUseCase = provideCheckInternetUseCase()
        return AgreementImpl(activity, checkInternetUseCase, failUiController)
    }

    fun provideTrackStorageHelper(context: Context): TrackStorageHelper {
        val sharedPrefs = context.getSharedPreferences("SelectedTrackPrefs", Context.MODE_PRIVATE)
        val gson = Gson()
        val serializer = GsonTrackSerializer(gson)
        return SharedPrefsTrackStorage(sharedPrefs, serializer)
    }

    fun provideMovieStorageHelper(context: Context): MovieStorageHelper {
        val prefs = context.getSharedPreferences("SelectedMoviePrefs", Context.MODE_PRIVATE)
        val serializer = GsonMovieSerializer(Gson())
        return SharedPrefsMovieStorage(prefs, serializer)
    }

    fun provideTrackSerializer(): TrackSerializer {
        return GsonTrackSerializer(Gson())
    }

    fun provideMovieSerializer(): MovieSerializer {
        return GsonMovieSerializer(Gson())
    }

    private fun provideNetworkRepository(): NetworkRepository {
        return NetworkRepositoryImpl(RetrofitInstance.api)
    }

    private fun provideCheckInternetUseCase(): CheckInternetConnectionUseCase {
        return CheckInternetConnectionUseCase(provideNetworkRepository())
    }

    fun provideNetworkStatusChecker(context: Context): NetworkStatusChecker {
        return NetworkStatusCheckerImpl(context)
    }

    fun provideResourceColorProvider(context: Context): ResourceColorProvider {
        return ResourceColorProviderImpl(context)
    }

    fun provideTrackListIntentParser(): TrackListIntentParser {
        return TrackListIntentParserImpl()
    }
// movie Favorite //  (❤️)
    private var localStorage: LocalStorage? = null
    private var favoritesRepository: FavoritesRepository? = null
    private var toggleFavoriteUseCase: ToggleFavoriteUseCase? = null

    private fun provideLocalStorage(context: Context): LocalStorage {
        if (localStorage == null) {
            val prefs = context.getSharedPreferences("prefs", Context.MODE_PRIVATE)
            localStorage = LocalStorage(prefs)
        }
        return localStorage!!
    }

    private fun provideFavoritesRepository(context: Context): FavoritesRepository {
        if (favoritesRepository == null) {
            favoritesRepository = FavoritesRepositoryImpl(provideLocalStorage(context))
        }
        return favoritesRepository!!
    }

    fun provideToggleFavoriteUseCase(context: Context): ToggleFavoriteUseCase {
        if (toggleFavoriteUseCase == null) {
            toggleFavoriteUseCase = ToggleFavoriteUseCase(provideFavoritesRepository(context))
        }
        return toggleFavoriteUseCase!!
    }

    fun provideMoviesViewModelFactory(context: Context): MoviesViewModelFactory {
        return MoviesViewModelFactory(
            provideMoviesInteraction(),
            provideToggleFavoriteUseCase(context)
        )
    }
}
