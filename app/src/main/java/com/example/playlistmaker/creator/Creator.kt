package com.example.playlistmaker.creator

import MoviesInteraction
import MoviesRepositoryImpl
import android.app.Activity
import android.content.Context
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.playlistmaker.data.network.ForecaApi
import com.example.playlistmaker.data.network.IMDbApi
import com.example.playlistmaker.data.network.ITunesApi
import com.example.playlistmaker.data.network.RetrofitAudioNetworkClient
import com.example.playlistmaker.data.network.RetrofitInstance
import com.example.playlistmaker.data.network.RetrofitWeatherNetworkClient
import com.example.playlistmaker.data.repository.AgreementImpl
import com.example.playlistmaker.data.repository.AudioRepositoryImpl
import com.example.playlistmaker.data.repository.AudioSingleTrackImpl
import com.example.playlistmaker.data.repository.AudioTracksImpl
import com.example.playlistmaker.data.repository.GsonMovieSerializer
import com.example.playlistmaker.data.repository.GsonTrackSerializer
import com.example.playlistmaker.data.repository.LanguageRepositoryImpl
import com.example.playlistmaker.data.repository.NavigationUseCaseImpl
import com.example.playlistmaker.data.repository.NetworkRepositoryImpl
import com.example.playlistmaker.data.repository.NetworkStatusCheckerImpl
import com.example.playlistmaker.data.repository.ResourceColorProviderImpl
import com.example.playlistmaker.data.repository.ShareImpl
import com.example.playlistmaker.data.repository.ShareMovieImpl
import com.example.playlistmaker.data.repository.SharedPreferencesSearchHistoryRepository
import com.example.playlistmaker.data.repository.SharedPrefsMovieStorage
import com.example.playlistmaker.data.repository.SharedPrefsTrackStorage
import com.example.playlistmaker.data.repository.SupportImpl
import com.example.playlistmaker.data.repository.ThemeRepositoryImpl
import com.example.playlistmaker.data.repository.TrackListIntentParserImpl
import com.example.playlistmaker.data.repository.WeatherRepositoryImpl
import com.example.playlistmaker.domain.api.AudioInteraction
import com.example.playlistmaker.domain.api.AudioNetworkClient
import com.example.playlistmaker.domain.api.AudioPlayerInteraction
import com.example.playlistmaker.domain.api.AudioRepository
import com.example.playlistmaker.domain.api.LanguageInteraction
import com.example.playlistmaker.domain.api.MovieSerializer
import com.example.playlistmaker.domain.api.MovieStorageHelper
import com.example.playlistmaker.domain.api.MoviesRepository
import com.example.playlistmaker.domain.api.NavigationUseCase
import com.example.playlistmaker.domain.api.NetworkStatusChecker
import com.example.playlistmaker.domain.api.SearchHistoryInteraction
import com.example.playlistmaker.domain.api.ThemeInteraction
import com.example.playlistmaker.domain.api.TrackSerializer
import com.example.playlistmaker.domain.api.TrackStorageHelper
import com.example.playlistmaker.domain.api.WeatherInteraction
import com.example.playlistmaker.domain.api.WeatherRepository
import com.example.playlistmaker.domain.api.weatherNetworkClient
import com.example.playlistmaker.domain.impl.AudioInteractionImpl
import com.example.playlistmaker.domain.impl.AudioPlayerInteractionImpl
import com.example.playlistmaker.domain.impl.LanguageInteractionImpl
import com.example.playlistmaker.domain.impl.MoviesInteractionImpl
import com.example.playlistmaker.domain.impl.SearchHistoryInteractionImpl
import com.example.playlistmaker.domain.impl.ThemeInteractionImpl
import com.example.playlistmaker.domain.impl.WeatherInteractionImpl
import com.example.playlistmaker.domain.repository.Agreement
import com.example.playlistmaker.domain.repository.AudioSingleTrackShare
import com.example.playlistmaker.domain.repository.AudioTracksShare
import com.example.playlistmaker.domain.repository.NetworkRepository
import com.example.playlistmaker.domain.repository.ResourceColorProvider
import com.example.playlistmaker.domain.repository.Share
import com.example.playlistmaker.domain.repository.ShareMovie
import com.example.playlistmaker.domain.repository.Support
import com.example.playlistmaker.domain.repository.TrackListIntentParser
import com.example.playlistmaker.domain.usecases.CheckInternetConnectionUseCase
import com.example.playlistmaker.presentation.audioPostersViewModels.ExtraOptionViewModelFactory
import com.example.playlistmaker.presentation.audioViewModels.SearchViewModelFactory
import com.example.playlistmaker.presentation.mainViewModels.MainViewModel
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
}
