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
    private var bottomViewState = 0

    fun setupBottomNavigation() {

        bottomViewIds.forEachIndexed { index, bottomViewId ->
            val bottomView: TextView? = activity.findViewById(bottomViewId)

            if (bottomView != null && index < buttonPairs.size) {
                bottomView.text = buttonPairs[index].first
                bottomView.setCompoundDrawablesWithIntrinsicBounds(0, buttonPairs[index].second, 0, 0)

                bottomView.setOnClickListener {
                    val navigationData = navigationList[index]

                    when (navigationData) {
                        is NavigationData.ActivityData -> {
                            if (activity::class.java == navigationData.activityClass) {
                                val currentButtonIndex = this.buttonIndex
                                val newButtonIndex = navigationData.buttonIndex

                                if (currentButtonIndex != newButtonIndex && activity is MainActivity) {
                                    this.buttonIndex = newButtonIndex ?: 0
                                    (activity as MainActivity).switchFragment(buttonIndex)
                                    selectButton(buttonIndex)
                                    setBottomNavigationVisibility()
                                } else {
                                    updateVisibilityForButtons(index)
                                    bottomViewState = if (bottomViewState == 0) 1 else 0
                                }
                            } else {
                                buttonIndex = index
                                launchActivity(navigationData)
                            }
                        }

                        is NavigationData.FragmentData -> {
                            buttonIndex = index
                            showFragment(navigationData)
                        }
                    }
                }
            }
        }
    }

    private val bottomViews: List<TextView> by lazy {
        bottomViewIds.mapNotNull { id -> activity.findViewById<TextView>(id) }
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

//    private fun showFragment(data: NavigationData.FragmentData) {
//        val fragment = data.fragmentProvider()
//
//        activity.supportFragmentManager.beginTransaction()
//            .setCustomAnimations(data.enterAnim, data.exitAnim)
//            .replace(R.id.rootContainer, fragment, data.tag)
//            .commit()
//    }

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

    private fun updateVisibilityForButtons(num: Int) {
        val isBelowThree = num < 3
        val visibleIndices = if (isBelowThree) {
            if (bottomViewState == 0) arrayOf(num, 3, 4, 5)
            else arrayOf(0, 1, 2)
        } else {
            if (bottomViewState == 0) arrayOf(0, 1, 2, num)
            else arrayOf(3, 4, 5)
        }

        bottomViewIds.forEachIndexed { index, viewId ->
            val bottomView: TextView? = activity.findViewById(viewId)
            bottomView?.visibility = if (index in visibleIndices) View.VISIBLE else View.GONE
        }
    }

    fun setBottomNavigationVisibility() {

        bottomViewIds.forEachIndexed { index, viewId ->
            val bottomView: TextView? = activity.findViewById(viewId)
            val visibility = when {
                buttonIndex in 0..2 && index in 0..2 -> View.VISIBLE
                buttonIndex in 3..5 && index in 3..5 -> View.VISIBLE
                else -> View.GONE
            }

            bottomView?.visibility = visibility
        }
    }

    private fun setNavigationLineVisibility(visibility: Int) {
        val navigationLine: View = activity.findViewById(R.id.navigationLine)
        navigationLine.visibility = visibility
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
        val bottomNavigation: LinearLayout = activity.findViewById(R.id.bottom_navigation)
        bottomNavigation.visibility = visibility
    }

    fun selectButton(index: Int) {
        this.buttonIndex = index  // <-- добавлено: обновляем внутренний индекс
        bottomViews.forEachIndexed { i, view ->
            view.isSelected = i == index
        }
    }
}