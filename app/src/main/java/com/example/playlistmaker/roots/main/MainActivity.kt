package com.example.playlistmaker.roots.main

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.example.playlistmaker.App
import com.example.playlistmaker.BaseActivity
import com.example.playlistmaker.BaseFragment
import com.example.playlistmaker.R
import com.example.playlistmaker.presentation.utils.BottomNavigationHelper
import com.example.playlistmaker.presentation.utils.NavigationConfigProvider
import com.example.playlistmaker.ui.audio.ReversableList
import com.example.playlistmaker.ui.audio.SearchFragment
import com.example.playlistmaker.ui.chat.UsersRepo
import com.example.playlistmaker.ui.media.MediaLibraryFragment
import com.example.playlistmaker.ui.settings.SettingsFragment
import com.example.playlistmaker.utils.ACTION_SHOW_IMPORT_PREVIEW
import com.example.playlistmaker.utils.ARG_IMPORT_URI
import com.example.playlistmaker.utils.EXTRA_IMPORT_ENTRY
import com.example.playlistmaker.utils.EXTRA_IMPORT_URI
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.auth
import com.google.firebase.firestore.ktx.firestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class MainActivity : BaseActivity() {

    private val requestPostNotifications =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            // можно показать подсказку, если не выдали
        }

    private val prefs by lazy { getSharedPreferences("chat_prefs", MODE_PRIVATE) }
    private fun wasProfileAskedOnce() = prefs.getBoolean("profile_asked_once", false)
    @SuppressLint("UseKtx")
    private fun markProfileAskedOnce() = prefs.edit().putBoolean("profile_asked_once", true).apply()

    private fun ensureFirebaseAuthThen(onReady: (String) -> Unit) {
        val auth = com.google.firebase.Firebase.auth
        val current = auth.currentUser
        if (current != null) {
            // уже вошли — создадим/обновим запись в users и поехали
            lifecycleScope.launch {
                try { UsersRepo().ensureCurrentUser(current.uid) } catch (_: Exception) {}
                // >>> добавлен Push Notification Token Issue
                com.example.playlistmaker.ui.chat.notification.FcmTokenManager
                    .forceRegisterFcmToken(this@MainActivity)
                // <<<
                onReady(current.uid)
            }
            return
        }

        auth.signInAnonymously()
            .addOnSuccessListener { res ->
                val uid = res.user?.uid ?: return@addOnSuccessListener
                lifecycleScope.launch {
                    try { UsersRepo().ensureCurrentUser(uid) } catch (_: Exception) {}
                    // >>> добавлен Push Notification Token Issue
                    com.example.playlistmaker.ui.chat.notification.FcmTokenManager
                        .forceRegisterFcmToken(this@MainActivity)
                    // <<<
                    onReady(uid)
                }
            }
            .addOnFailureListener { e ->
                android.util.Log.e("AUTH", "Anon sign-in failed", e)
            }
    }

    private var enteredFromImport = false
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Android 13+: запросить разрешение на уведомления
        if (Build.VERSION.SDK_INT >= 33) {
            requestPostNotifications.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        }

        // 1) (как было) разбор import-интента и т.д. — ОК
        enteredFromImport = savedInstanceState?.getBoolean("enteredFromImport")
            ?: intent.getBooleanExtra(EXTRA_IMPORT_ENTRY, false)

        // 2) СНАЧАЛА получаем navController
        navController = (supportFragmentManager
            .findFragmentById(R.id.nav_host_container) as NavHostFragment).navController

        // 3) ТЕПЕРЬ вызываем аутентификацию и проверку профиля
        ensureFirebaseAuthThen { uid ->
            android.util.Log.d("AUTH", "signed in as $uid")

            // показываем профиль только ОДИН раз после самого первого входа
            if (!wasProfileAskedOnce()) {
                // пробуем прочитать имя — если пустое, тоже ок, просто откроем профиль
                lifecycleScope.launch {
                    try {
                        val doc = com.google.firebase.ktx.Firebase.firestore
                            .collection("profiles")
                            .document(uid)
                            .get()
                            .await()

                    // не важно, какое там имя — мы всё равно откроем профиль лишь один раз
                    } catch (_: Throwable) { /* игнор */ }

                    // открыть профиль и сразу пометить, что уже спрашивали
                    if (navController.currentDestination?.id != R.id.profileFragment) {
                        navController.navigate(R.id.profileFragment)
                    }
                    markProfileAskedOnce()
                }
            }
        }

        // 4) Дальше — существующая логика вычисления buttonIndex и стартовой навигации
        buttonIndex = when {
            savedInstanceState != null -> {
                // Восстановление после recreate()
                val restored = savedInstanceState.getInt("buttonIndex", 1)
                restored
            }
            !App.wasInitialLaunchDone -> {
                App.wasInitialLaunchDone = true
                1
            }
            else -> {
                val resolved = intent.getIntExtra("buttonIndex", 1)
                resolved
            }
        }

        // Обновляем Intent, чтобы сохранить index при recreate()
        intent.putExtra("buttonIndex", buttonIndex)

        // ✅ 1 onCreate() для перехода с TrackPreviewFragment на CreatePlaylistFragment :
        // Сначала пробуем обработать входящий интент
        val handled = handleExternalIntentOnce(intent)

        // Если интент НЕ был импортом/внешним кейсом — делаем стартовую навигацию
        if (savedInstanceState == null && !handled) {
            val destinationId = when (buttonIndex) {
                0 -> R.id.searchFragment
                1 -> R.id.mediaLibraryFragment
                2 -> R.id.settingsFragment
                5 -> R.id.usersFragment  // ✅ добавляем
                else -> R.id.mainFragment
            }
//            5 -> R.id.extraOptionFragment // ✅ добавляем

            if (navController.currentDestination?.id != destinationId) {
                navController.navigate(destinationId)
            }
        }

        // ✅ Создаём списки и выводим лог — для отладки
        val bottomViewIds = listOf(
            R.id.bottom1, R.id.bottom2, R.id.bottom3,
            R.id.bottom4, R.id.bottom5, R.id.bottom6
        )

        val buttonPairs = NavigationConfigProvider.getButtonPairs(this)
        val navigationList = NavigationConfigProvider.getNavigationList()

        // ✅ Инициализируем BottomNavigationHelper
        bottomNavigationHelper = BottomNavigationHelper(
            activity = this,
            bottomViewIds = bottomViewIds,
            buttonPairs = buttonPairs,
            navigationList = navigationList,
            buttonIndex = buttonIndex
        )

        // ✅ Настраиваем нижнюю навигацию
        bottomNavigationHelper.setupBottomNavigation()
        if (!handled) {
            bottomNavigationHelper.selectButton(buttonIndex)  // как было
        }
        bottomNavigationHelper.setBottomNavigationVisibility()

        // for dialog on exit
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (enteredFromImport) finishAffinity() else handleBack()      //  обычная логика
            }
        })

        // for playlistInfoFragment on sprint 23 + importPreviewFragment для просмотра альбома
        navController.addOnDestinationChangedListener { _, dest, _ ->
            val hideOn = setOf(
                R.id.playlistInfoFragment, // сюда можно добавить и другие экраны без нижней навигации
                R.id.importPreviewFragment
            )
            val shouldHide = dest.id in hideOn

            val isMainDestination = dest.id == R.id.mainFragment // подставь ID твоего главного фрагмента

            if (isMainDestination) {
                toolbarHelper.applyMainBlueColors()
            } else {
                toolbarHelper.applyThemeColors()
            }

            // прячем кастомный низ
            findViewById<View>(R.id.bottomNavigation).isVisible = !shouldHide
            // если нужно — прячем и кастомный тулбар
            findViewById<View>(R.id.toolbar)?.isVisible = !shouldHide
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt("buttonIndex", buttonIndex)
        outState.putBoolean("enteredFromImport", enteredFromImport)   // ← добавили
    }

    fun switchFragment(index: Int) {
        buttonIndex = index

        val destinationId = when (index) {
            0 -> R.id.searchFragment
            1 -> R.id.mediaLibraryFragment
            2 -> R.id.settingsFragment
            5 -> R.id.usersFragment // ✅ добавляем
            else -> R.id.mainFragment
        }
//        5 -> R.id.extraOptionFragment // ✅ добавляем

        if (navController.currentDestination?.id != destinationId) {
            navController.navigate(destinationId)
        }
    }

    override fun onSegment4Clicked() {
        val fragment = getCurrentVisibleFragment()
        if (fragment is BaseFragment) {
            fragment.onSegment4ClickedInternal()
        }
    }

    private fun getCurrentNavHostFragment(): Fragment? {
        return supportFragmentManager.findFragmentById(R.id.nav_host_container)
    }

    private fun getCurrentVisibleFragment(): Fragment? {
        val navHostFragment = getCurrentNavHostFragment() as? NavHostFragment
        return navHostFragment?.childFragmentManager?.primaryNavigationFragment
    }

    override fun reverseList() {
        val fragment = getCurrentVisibleFragment()
        if (fragment is ReversableList) {
            fragment.reverseList()
        }
    }

    override fun shouldEnableEdgeToEdge(): Boolean = false

    private fun handleBack() {

        val fromImport = intent.getBooleanExtra(EXTRA_IMPORT_ENTRY, false)
        if (fromImport) {
            finish() // закрыть документ → возврат в Gmail
            return
        }

        val current = getCurrentVisibleFragment()
            val isTopLevel =
                current is SearchFragment ||
                        current is MediaLibraryFragment ||
                        current is SettingsFragment

            if (isTopLevel) {
                // Диалог подтверждения выхода
                MaterialAlertDialogBuilder(this)
                    .setMessage("Вы действительно хотите выйти из приложения?")
                    .setPositiveButton("Да") { d, _ ->
                        d.dismiss()
                        finishAffinity() // закрываем всю задачу приложения
                    }
                    .setNegativeButton("Нет") { d, _ -> d.dismiss() }
                    .show()
            } else {
                // Обычный шаг назад по навграфу
                if (!navController.navigateUp()) {
                    // Если уже некуда «назад» внутри графа — закрываем текущую Activity
                    finish()
                }
            }
    }

    // ✅ 2 для перехода с TrackPreviewFragment на CreatePlaylistFragment :
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleExternalIntentOnce(intent)
    }

    // ✅ 3 для перехода с TrackPreviewFragment на CreatePlaylistFragment : (приватный хелпер):
    private fun handleExternalIntent(intent: Intent?): Boolean {
        if (intent == null) return false

        // ----- 1) Открыть CreatePlaylistFragment -----
        if (intent.getBooleanExtra("open_create_playlist", false)) {
            val fromPreview  = intent.getBooleanExtra("return_result", false)
            val focusTrackId = intent.getLongExtra("preview_track_id", -1L).takeIf { it > 0 }

            val args = Bundle().apply {
                putBoolean("from_preview", fromPreview)
                focusTrackId?.let { putLong("preview_track_id", it) }
            }

            if (navController.graph.findNode(R.id.createPlaylistFragment) != null) {
                navController.navigate(R.id.createPlaylistFragment, args)
            }

            // очистить, чтобы не навигировалось повторно
            intent.removeExtra("open_create_playlist")
            intent.removeExtra("return_result")
            intent.removeExtra("preview_track_id")
            return true
        }

        // ----- 2) Показать ImportPreviewFragment -----
        val isImportAction =
            intent.action == ACTION_SHOW_IMPORT_PREVIEW ||
                    (intent.action == Intent.ACTION_VIEW && (
                            intent.type == "application/zip" ||
                                    intent.data?.lastPathSegment?.endsWith(".plz", true) == true ||
                                    intent.data?.lastPathSegment?.endsWith(".zip", true) == true
                            ))

        if (isImportAction) {
            val uri: Uri? = intent.extras?.let {
                androidx.core.os.BundleCompat.getParcelable(it, EXTRA_IMPORT_URI, Uri::class.java)
            } ?: intent.data ?: intent.clipData?.getItemAt(0)?.uri

            if (uri != null) {
                val args = bundleOf(ARG_IMPORT_URI to uri)

                val options = androidx.navigation.navOptions {
                    launchSingleTop = true
                    // <-- ключ: делаем importPreview единственным в back stack
                    popUpTo(navController.graph.id) { inclusive = true }
                }

                // +++ Новое: запоминаем, что зашли «из импорта»
                enteredFromImport = true

                if (navController.currentDestination?.id != R.id.importPreviewFragment) {
                    navController.navigate(R.id.importPreviewFragment, args, options)
                }

                // помечаем, что это было внешнее открытие
                intent.putExtra(EXTRA_IMPORT_ENTRY, true)
                intent.putExtra("_consumed", true)

                // очистка, чтобы не повторялось
                intent.action = null
                intent.removeExtra(EXTRA_IMPORT_URI)
                intent.data = null
                setIntent(intent)

                // _consumed и setIntent оставляем на handleExternalIntentOnce(...)
                return true
            }
        }
        return false
    }

    private fun handleExternalIntentOnce(intent: Intent?): Boolean {
        if (intent == null) return false
        if (intent.getBooleanExtra("_consumed", false)) return false
        val handled = handleExternalIntent(intent)
        if (handled) {
            intent.putExtra("_consumed", true)
            setIntent(intent) // обновляем текущий intent Activity
        }
        return handled
    }
}
