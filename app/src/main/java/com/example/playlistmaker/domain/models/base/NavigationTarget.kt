package com.example.playlistmaker.domain.models.base

import android.app.Activity
import android.os.Bundle

sealed class NavigationTarget {
    data class ActivityTarget(
        val activityClass: Class<out Activity>,
        val enterAnim: Int,
        val exitAnim: Int
    ) : NavigationTarget()

    data class FragmentTarget(
        val destinationId: Int,
        val args: Bundle? = null,
        val enterAnim: Int,
        val exitAnim: Int
    ) : NavigationTarget()

}