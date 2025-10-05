package com.example.playlistmaker

import android.annotation.SuppressLint
import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import android.view.View
import android.view.ViewGroup
import com.example.playlistmaker.di.appModule
import com.example.playlistmaker.di.db.databaseModule
import com.example.playlistmaker.di.extraOption.extraOptionDataModule
import com.example.playlistmaker.di.extraOption.extraOptionInteractionModule
import com.example.playlistmaker.di.extraOption.extraOptionViewModelModule
import com.example.playlistmaker.di.imageLoaderModule
import com.example.playlistmaker.di.mainActivity.mainActivityModule
import com.example.playlistmaker.di.media.mediaViewModelModule
import com.example.playlistmaker.di.movie.movieDataModule
import com.example.playlistmaker.di.movie.movieInteractionModule
import com.example.playlistmaker.di.movie.movieRepositoryModule
import com.example.playlistmaker.di.movie.movieViewModelModule
import com.example.playlistmaker.di.movie.namesInteractorModule
import com.example.playlistmaker.di.movie.namesRepositoryModule
import com.example.playlistmaker.di.navigation.navigationModule
import com.example.playlistmaker.di.playlist.playlistsModule
import com.example.playlistmaker.di.search.searchDataModule
import com.example.playlistmaker.di.search.searchInteractionModule
import com.example.playlistmaker.di.search.searchRepositoryModule
import com.example.playlistmaker.di.search.searchViewModelModule
import com.example.playlistmaker.di.settingsActivity.settingsActivityDataModule
import com.example.playlistmaker.di.settingsActivity.settingsActivityInteractionModule
import com.example.playlistmaker.di.settingsActivity.settingsActivityViewModelModule
import com.example.playlistmaker.di.weather.weatherDataModule
import com.example.playlistmaker.di.weather.weatherInteractionModule
import com.example.playlistmaker.di.weather.weatherRepositoryModule
import com.example.playlistmaker.domain.api.base.ThemeInteraction
import com.example.playlistmaker.presentation.utils.ThemeLanguageHelper
import org.koin.android.ext.android.get
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext.startKoin

// ☀️ 🔁 🌙 👉 🧼🏗️✅
class App : Application() { // ☀️ 🔁 🌙

    companion object {
        var wasInitialLaunchDone: Boolean = false

        // Если меняли важность и старый канал уже создан с MIN,
        // используем новый ID (и в серверном FCM).
        const val CHAT_CHANNEL_ID = "chat"
        const val CHAT_BADGE_CHANNEL_ID = "chat_badge" // или "chat_badge_v2"
    }

    @SuppressLint("ObsoleteSdkInt")
    override fun onCreate() {
        super.onCreate()

        // ВАЖНО: сначала создаём каналы
        ensureChatChannel(this)
        ensureChatBadgeChannel(this)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            // 1) канал для обычных чатов:
            val ch = NotificationChannel(
                "chat",
                "Чаты",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply { setShowBadge(true) }  // <- важное

            // 2) НОВЫЙ канал для «тихого» бейджа (фолбэк для Pixel/AOSP):
            val chBadge = NotificationChannel(
                "chat_badge",
                "Chat badge",
                NotificationManager.IMPORTANCE_MIN // без баннеров/звуков
            ).apply {
                setShowBadge(true)
                setSound(null, null)
                enableVibration(false)
                description = "Silent badge channel for app icon count"
            }


            val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.createNotificationChannel(ch)
            nm.createNotificationChannel(chBadge)
        }


        startKoin {
            androidContext(this@App)
            modules( // 👉 📝 🔄
                listOf(
                    appModule,
                    weatherDataModule,
                    weatherRepositoryModule,
                    weatherInteractionModule,
                    movieDataModule,
                    movieRepositoryModule,
                    movieInteractionModule,
                    movieViewModelModule,
                    searchDataModule,
                    searchRepositoryModule,
                    searchInteractionModule,
                    searchViewModelModule,
                    extraOptionDataModule,
                    extraOptionInteractionModule,
                    extraOptionViewModelModule,
                    settingsActivityDataModule,
                    settingsActivityInteractionModule,
                    settingsActivityViewModelModule,
                    mainActivityModule,
                    mediaViewModelModule,
                    navigationModule,
                    namesRepositoryModule,      // 👈 добавили
                    namesInteractorModule,      // 👈 добавили
                    imageLoaderModule,          // 👈 для Glide
                    databaseModule,
                    playlistsModule
                )
            )
        }

        // ⛳️ Обязательно: инициализация ThemeLanguageHelper ДО applySavedLanguage
        ThemeLanguageHelper.init(
            theme = get(),       // get<ThemeInteraction> из Koin
            language = get()     // get<LanguageInteraction> из Koin
        )

        // 🌍 Установка языка на основе сохранённого
        ThemeLanguageHelper.applySavedLanguage(this)

        // 3) применяем сохранённую тему (до показа первой Activity)
        get<ThemeInteraction>().applyTheme()

        // 🧼 Ловим крэш-ошибки
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            Log.e("UncaughtException", "Uncaught exception in thread ${thread.name}", throwable)
        }
    }

    private fun View.traverse(action: (View) -> Unit) {
        action(this)
        if (this is ViewGroup) {
            for (i in 0 until childCount) {
                getChildAt(i).traverse(action)
            }
        }
    }

    /** Обычные чат-уведомления (если понадобятся отдельным каналом) */
    @SuppressLint("ObsoleteSdkInt")
    fun ensureChatChannel(ctx: Context) {
        if (Build.VERSION.SDK_INT < 26) return
        val nm = ctx.getSystemService(NotificationManager::class.java)
        val existing = nm.getNotificationChannel(App.CHAT_CHANNEL_ID)
        if (existing == null) {
            val ch = NotificationChannel(
                App.CHAT_CHANNEL_ID,
                "Чаты",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                setShowBadge(true)
                // можно оставить звук по умолчанию или отключить:
                // setSound(null, null)
                // enableVibration(false)
                description = "Уведомления чатов"
            }
            nm.createNotificationChannel(ch)
        }
    }

    /**
     * Канал для «тихого» бейджа (точка/цифра).
     * Важность — DEFAULT (без звука/вибра), иначе на части лаунчеров точка не появится.
     */
    @SuppressLint("ObsoleteSdkInt")
    fun ensureChatBadgeChannel(ctx: Context) {
        if (Build.VERSION.SDK_INT < 26) return
        val nm = ctx.getSystemService(NotificationManager::class.java)
        val existing = nm.getNotificationChannel(App.CHAT_BADGE_CHANNEL_ID)

        // Важность нельзя менять после создания.
        // Если существующий канал с MIN — создайте канал с новым ID и используйте его.
        if (existing == null) {
            val ch = NotificationChannel(
                App.CHAT_BADGE_CHANNEL_ID,
                "Chat badge",
                NotificationManager.IMPORTANCE_DEFAULT // ключевой момент
            ).apply {
                setShowBadge(true)
                setSound(null, null)     // тихо
                enableVibration(false)   // без вибра
                description = "Канал для точки/цифры на иконке"
            }
            nm.createNotificationChannel(ch)
        }
    }
}
