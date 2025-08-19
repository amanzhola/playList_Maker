package com.example.playlistmaker.presentation.utils

import android.content.Intent
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityOptionsCompat
import androidx.navigation.fragment.findNavController
import androidx.navigation.navOptions
import com.example.playlistmaker.NavigationData
import com.example.playlistmaker.R
import com.example.playlistmaker.roots.main.MainActivity

class BottomNavigationHelper(
    private val activity: AppCompatActivity,
    private val bottomViewIds: List<Int>,
    private val buttonPairs: List<Pair<String, Int>>,
    private val navigationList: List<NavigationData>,
    private var buttonIndex: Int
) {
    // УБРАНО: bottomViewState и вся ручная чехарда с видимостью

    private val bottomViews: List<TextView> by lazy {
        bottomViewIds.mapNotNull { id -> activity.findViewById<TextView>(id) }
    }

    fun setupBottomNavigation() {
        bottomViewIds.forEachIndexed { index, bottomViewId ->
            val bottomView: TextView? = activity.findViewById(bottomViewId)
            if (bottomView != null && index < buttonPairs.size) {
                bottomView.text = buttonPairs[index].first
                bottomView.setCompoundDrawablesWithIntrinsicBounds(0, buttonPairs[index].second, 0, 0)

                bottomView.setOnClickListener {
                    val nav = navigationList[index]

                    // 1) Сначала — всегда синхронизируем «страницу» и подсветку
                    selectButton(
                        when (nav) {
                            is NavigationData.ActivityData -> nav.buttonIndex ?: index
                            is NavigationData.FragmentData -> index
                        }
                    )

                    // 2) Потом — навигация
                    when (nav) {
                        is NavigationData.ActivityData -> {
                            if (activity::class.java == nav.activityClass) {
                                // Уже в нужной Activity
                                if (activity is MainActivity) {
                                    activity.switchFragment(buttonIndex)
                                    setBottomNavigationVisibility()
                                }
                            } else {
                                launchActivity(nav)
                            }
                        }
                        is NavigationData.FragmentData -> {
                            showFragment(nav)
                        }
                    }
                }
            }
        }

        // Инициализация видимости/подсветки при старте
        selectButton(buttonIndex)
    }

    private fun launchActivity(data: NavigationData.ActivityData) {
        val intent = Intent(activity, data.activityClass).apply {
            putExtra("buttonIndex", data.buttonIndex)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        }
        val options = ActivityOptionsCompat.makeCustomAnimation(
            activity, data.enterAnim, data.exitAnim
        )
        activity.startActivity(intent, options.toBundle())
    }

    private fun showFragment(data: NavigationData.FragmentData) {
        val navHostFragment = activity.supportFragmentManager.findFragmentById(R.id.nav_host_container)
        val navController = navHostFragment?.findNavController() ?: return

        val options = navOptions {
            anim {
                enter = data.enterAnim
                exit = data.exitAnim
                popEnter = data.enterAnim
                popExit = data.exitAnim
            }
        }
        navController.navigate(data.destinationId, data.args, options)
    }

    fun setBottomNavigationVisibility() {
        bottomViewIds.forEachIndexed { index, viewId ->
            val bottomView: TextView? = activity.findViewById(viewId)
            val visible = when {
                buttonIndex in 0..2 && index in 0..2 -> View.VISIBLE
                buttonIndex in 3..5 && index in 3..5 -> View.VISIBLE
                else -> View.GONE
            }
            bottomView?.visibility = visible
        }
    }

    private fun setNavigationLineVisibility(visibility: Int) {
        activity.findViewById<View>(R.id.navigationLine)?.visibility = visibility
    }

    fun showBottomNavigation() {
        setBottomNavigationContainerVisibility(View.VISIBLE)
        setNavigationLineVisibility(View.VISIBLE)
    }

    fun hideBottomNavigation() {
        setBottomNavigationContainerVisibility(View.GONE)
        setNavigationLineVisibility(View.GONE)
    }

    private fun setBottomNavigationContainerVisibility(visibility: Int) {
        activity.findViewById<LinearLayout>(R.id.bottom_navigation)?.visibility = visibility
    }

    /** ЕДИНАЯ точка правды: и страница, и подсветка */
    fun selectButton(index: Int) {
        if (index !in bottomViews.indices) return
        buttonIndex = index
        setBottomNavigationVisibility()             // ← показ нужной тройки
        bottomViews.forEachIndexed { i, view ->
            view.isSelected = i == index            // ← подсветка нужной кнопки
        }
    }

    /** по желанию: отдавай наружу текущий индекс (чтобы Activity могла сохранять/восстанавливать) */
    fun currentIndex(): Int = buttonIndex
}
