package com.example.playlistmaker.presentation.utils.activityHelper

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.app.ActivityCompat.recreate
import androidx.recyclerview.widget.RecyclerView
import com.example.playlistmaker.ui.audio.SearchActivity
import com.example.playlistmaker.ui.main.MainActivity
import com.example.playlistmaker.ui.settings.SettingsActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.switchmaterial.SwitchMaterial

class ActivityUiHider( // 🧪
    private val activity: Activity,
    private val mainLayout: ViewGroup
) {

    fun hideContent() {
        updateViews(visible = false)
    }

    fun restoreContent() {
        updateViews(visible = true)
        recreate(activity) // under test for main buttons back
    }

    private fun updateViews(visible: Boolean) {
        for (i in 0 until mainLayout.childCount) {
            val view = mainLayout.getChildAt(i)
            val shouldAffect = when {
                view is MaterialButton && activity is MainActivity -> true
                (view is TextView || view is SwitchMaterial) && activity is SettingsActivity -> true // 🤔 😬
                view is RecyclerView && activity is SearchActivity -> true
                else -> false
            }
            if (shouldAffect) {
                view.visibility = if (visible) View.VISIBLE else View.INVISIBLE
            }
        }
    }
}
