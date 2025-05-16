package com.example.playlistmaker.presentation.utils

import android.app.Activity
import android.content.Intent
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.app.ActivityOptionsCompat
import com.example.playlistmaker.NavigationData
import com.example.playlistmaker.R

class BottomNavigationHelper(
    private val activity: Activity,
    private val bottomViewIds: List<Int>,
    private val buttonPairs: List<Pair<String, Int>>,
    private val navigationList: List<NavigationData>,
    private var buttonIndex: Int
) {
    private var bottomViewState = 0

    fun setupBottomNavigation() { // 🥰

        bottomViewIds.forEachIndexed { index, bottomViewId ->
            val bottomView: TextView? = activity.findViewById(bottomViewId)

            if (bottomView != null && index < buttonPairs.size) {
                bottomView.text = buttonPairs[index].first // ☝️
                bottomView.setCompoundDrawablesWithIntrinsicBounds(0, buttonPairs[index].second, 0, 0)

                bottomView.setOnClickListener {
                    val navigationData = navigationList[index]
                    if (activity::class.java == navigationData.activityClass) {
                        updateVisibilityForButtons(index)
                        bottomViewState = if (bottomViewState == 0) 1 else 0
                    } else {
                        buttonIndex = index
                        launchActivity(navigationData)
                    }
                }
            }
        }
    }

    private fun launchActivity(navigationData: NavigationData) { //  🙋
        val intent = Intent(activity, navigationData.activityClass).apply {
            putExtra("buttonIndex", buttonIndex)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        }

        val options = ActivityOptionsCompat.makeCustomAnimation(
            activity, navigationData.enterAnim, navigationData.exitAnim
        )
        activity.startActivity(intent, options.toBundle())
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
            bottomView?.visibility = when {
                buttonIndex in 0..2 && index in 0..2 -> View.VISIBLE
                buttonIndex in 3..5 && index in 3..5 -> View.VISIBLE
                else -> View.GONE
            }
        }
    }

//    fun showBottomNavigation() {
//        setBottomNavigationVisibility()
//        setNavigationLineVisibility(View.VISIBLE)
//    }
//
//    fun hideBottomNavigation() {
//        setBottomNavigationVisibility(View.GONE)
//        setNavigationLineVisibility(View.GONE)
//    }
//
//    private fun setBottomNavigationVisibility(visibility: Int) {
//        val bottomNavigation: LinearLayout = activity.findViewById(R.id.bottom_navigation)
//        bottomNavigation.visibility = visibility
//    }

    private fun setNavigationLineVisibility(visibility: Int) {
        val navigationLine: View = activity.findViewById(R.id.navigationLine)
        navigationLine.visibility = visibility
    }
    // В BottomNavigationHelper
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
}
