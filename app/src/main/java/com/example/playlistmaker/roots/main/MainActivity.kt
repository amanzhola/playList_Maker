package com.example.playlistmaker.roots.main

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
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
import com.example.playlistmaker.ui.media.MediaLibraryFragment
import com.example.playlistmaker.ui.settings.SettingsFragment

class MainActivity : BaseActivity() {

    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        navController = (supportFragmentManager
            .findFragmentById(R.id.nav_host_container) as NavHostFragment).navController


        // Получаем переданный индекс (из Intent)
//        val passedIndex = intent.getIntExtra("buttonIndex", -1)

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

        if (savedInstanceState == null) {
            val destinationId = when (buttonIndex) {
                0 -> R.id.searchFragment
                1 -> R.id.mediaLibraryFragment
                2 -> R.id.settingsFragment
                else -> R.id.mainFragment
            }
            navController.navigate(destinationId)
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
        bottomNavigationHelper.selectButton(buttonIndex)
        bottomNavigationHelper.setBottomNavigationVisibility()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt("buttonIndex", buttonIndex)
    }

    fun switchFragment(index: Int) {
        buttonIndex = index

        val destinationId = when (index) {
            0 -> R.id.searchFragment
            1 -> R.id.mediaLibraryFragment
            2 -> R.id.settingsFragment
            else -> R.id.mainFragment
        }

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

    @Deprecated("This method has been deprecated in favor of using the\n      {@link OnBackPressedDispatcher} via {@link #getOnBackPressedDispatcher()}.\n      The OnBackPressedDispatcher controls how back button events are dispatched\n      to one or more {@link OnBackPressedCallback} objects.")
    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {
        // Проверка: если отображается один из корневых фрагментов — выходим из приложения
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_container)
        val currentFragment = navHostFragment?.childFragmentManager?.fragments?.firstOrNull()

        if (currentFragment is SearchFragment ||
            currentFragment is MediaLibraryFragment ||
            currentFragment is SettingsFragment
        ) {
            // Закрываем приложение
            finishAffinity() // ← завершает всё приложение
        }
        else {
            // Обычное поведение (вернуться назад)
//            super.onBackPressed()
            onBackPressedDispatcher.onBackPressed()
        }
    }
}
