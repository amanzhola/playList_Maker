package com.example.playlistmaker.presentation.utils.activityHelper

//import com.example.playlistmaker.ui.audio.SearchActivity
import android.app.Activity
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.app.ActivityCompat.recreate
import androidx.recyclerview.widget.RecyclerView
import com.example.playlistmaker.BaseActivity
import com.example.playlistmaker.ui.audio.SearchFragment
import com.example.playlistmaker.ui.main.MainFragment
import com.example.playlistmaker.ui.settings.SettingsFragment
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

        val currentFragment = (activity as? BaseActivity)?.getCurrentFragment()
        val isSearchFragment = currentFragment is SearchFragment
        val isSettingsFragment = currentFragment is SettingsFragment
        val isMainFragment = currentFragment is MainFragment

        for (i in 0 until mainLayout.childCount) {
            val view = mainLayout.getChildAt(i)

            val shouldAffect = when {
//                view is MaterialButton && activity is MainActivity -> true
                view is MaterialButton && isMainFragment -> true
                (view is TextView || view is SwitchMaterial) && isSettingsFragment -> true
                view is RecyclerView && isSearchFragment -> true
                else -> false
            }

            if (shouldAffect) {
                view.visibility = if (visible) View.VISIBLE else View.INVISIBLE
            }
        }

    }
}
