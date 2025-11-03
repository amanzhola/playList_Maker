package com.example.playlistmaker.di

import android.app.Activity
import android.content.Context
import android.util.Log
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.playlistmaker.data.network.base.RetrofitInstance
import com.example.playlistmaker.data.repository.base.AgreementImpl
import com.example.playlistmaker.data.repository.base.LanguageRepositoryImpl
import com.example.playlistmaker.data.repository.base.NetworkRepositoryImpl
import com.example.playlistmaker.data.repository.base.NetworkStatusCheckerImpl
import com.example.playlistmaker.data.repository.base.ResourceColorProviderImpl
import com.example.playlistmaker.data.repository.base.ShareImpl
import com.example.playlistmaker.data.repository.base.ShareMovieImpl
import com.example.playlistmaker.data.repository.base.SupportImpl
import com.example.playlistmaker.domain.api.base.LanguageInteraction
import com.example.playlistmaker.domain.api.base.NetworkStatusChecker
import com.example.playlistmaker.domain.impl.base.LanguageInteractionImpl
import com.example.playlistmaker.domain.repository.base.Agreement
import com.example.playlistmaker.domain.repository.base.LanguageRepository
import com.example.playlistmaker.domain.repository.base.NetworkRepository
import com.example.playlistmaker.domain.repository.base.ResourceColorProvider
import com.example.playlistmaker.domain.repository.base.Share
import com.example.playlistmaker.domain.repository.base.ShareMovie
import com.example.playlistmaker.domain.repository.base.Support
import com.example.playlistmaker.domain.usecases.base.CheckInternetConnectionUseCase
import com.example.playlistmaker.presentation.launcherViewModels.TrackPreviewViewModel
import com.example.playlistmaker.presentation.searchPostersViewModels.ExoPlayerProvider
import com.example.playlistmaker.presentation.searchPostersViewModels.ExoPlayerProviderImpl
import com.example.playlistmaker.presentation.utils.AudioErrorManager
import com.example.playlistmaker.presentation.utils.ColorPersistenceHelper
import com.google.android.material.button.MaterialButton
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.parameter.parametersOf
import org.koin.dsl.module

val appModule = module {

    Log.d("KOIN", "DI started")
    // BaseActivity **************************************************

    // 1 Repository (SharedPreferences) // 🅰️ 🌓 ↔️ 🌗 😎 from ⬇️ // Theme — для SettingsActivity // 🌓 ↔️ 🌗
    // 2 ThemeInteraction //🅱️ 🌓 ↔️ 🌗 😎 from ⬇️ // Theme — для SettingsActivity // 🌓 ↔️ 🌗

    // replace ColorPersistenceHelper 🎨📦
    // toolbar save and apply background color 🎨📦
    factory { (isDarkTheme: Boolean) ->
        ColorPersistenceHelper(androidContext(), isDarkTheme)
    }

    // replace share  // 👨‍💻
//    factory<Share> { (activity: Activity) -> ShareImpl(activity)}
    factory<Share> { (activity: Activity) ->
        ShareImpl(
            activity = activity,
            networkStatusChecker = get<NetworkStatusChecker> { parametersOf(androidContext()) }
        )
    }

    // replace support 🔧✨
    factory<Support> { (activity: Activity) ->
        SupportImpl(
            activity = activity,
            checkInternetConnectionUseCase = get()
        )
    }

    // + for replace agreement
    single<NetworkRepository> {
        NetworkRepositoryImpl(
            apiService = RetrofitInstance.api,
            networkStatusChecker = get<NetworkStatusChecker> { parametersOf(androidContext()) }
        )
    }

    // + for replace agreement
    single { CheckInternetConnectionUseCase(get()) }

    // replace agreement 👨‍💻 📜
    factory<Agreement> { (activity: Activity) ->
        AgreementImpl(
            activity = activity,
            checkInternetConnectionUseCase = get()   // get<CheckInternetConnectionUseCase>()
        )
    }

    // Язык — через SharedPreferences to re-do object to class ThemeLanguageHelper and replace val interaction = Creator.provideLanguageInteraction()
    single<LanguageRepository> { LanguageRepositoryImpl(androidContext()) } // provideLanguageInteraction()
    single<LanguageInteraction> { LanguageInteractionImpl(get()) } // provideLanguageInteraction()

    // Movie Sharing 👨‍💻 ⬇️ + replace ShareMovie in MoviesAdapterList and in MoviePagerList
    factory<ShareMovie> { (activity: Activity) ->
        ShareMovieImpl(
            activity = activity,
            networkStatusChecker = get<NetworkStatusChecker> { parametersOf(androidContext()) }
        )
    }

    // SearchActivity and TrackAdapter // 🌐 📶
    factory<NetworkStatusChecker> { (context: Context) ->
        NetworkStatusCheckerImpl(context)
    }

    // color for SearchAdapter 🎨
    factory<ResourceColorProvider> { (context: Context) ->
        ResourceColorProviderImpl(context)
    }

    // Error Manager 📜 👉 📝
    factory { (textView: TextView, update: MaterialButton, recyclerView: RecyclerView) ->
        AudioErrorManager(textView, update, recyclerView)
    } // 🧼 🔁  📝

    // TrackPreviewActivity **************************************************

    // updated sprint22 + 📜 👉 📝 oberver and bottom sheet + favourite ❤️
    viewModel { TrackPreviewViewModel(get(), get(), get(), get()) }

    // провайдер: Utube
    factory<ExoPlayerProvider> { ExoPlayerProviderImpl(androidContext().applicationContext) }
}
