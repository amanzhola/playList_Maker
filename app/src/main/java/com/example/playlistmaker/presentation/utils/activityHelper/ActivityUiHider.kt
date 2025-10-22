package com.example.playlistmaker.presentation.utils.activityHelper

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import com.example.playlistmaker.BaseActivity
import com.example.playlistmaker.R
import com.example.playlistmaker.ui.audio.SearchFragment
import com.example.playlistmaker.ui.main.MainFragment
import com.example.playlistmaker.ui.settings.SettingsFragment

class ActivityUiHider( // 🧪
    private val activity: Activity,
    private val mainLayout: ViewGroup
) {

    fun hideContent()  = updateViews(visible = false)
    fun restoreContent() = updateViews(visible = true)

    private fun updateViews(visible: Boolean) {
        val currentFragment = (activity as? BaseActivity)?.getCurrentFragment()
        val isSearchFragment   = currentFragment is SearchFragment
        val isSettingsFragment = currentFragment is SettingsFragment
        val isMainFragment     = currentFragment is MainFragment

        traverse(mainLayout) { view ->
            // не трогаем нижнюю навигацию
            if (view.id == R.id.bottomNavigation || view is com.google.android.material.bottomnavigation.BottomNavigationView) {
                return@traverse
            }

            if (isSettingsFragment) {
                // тумблеры (SwitchMaterial/Compat/и т.п.)
                (view as? android.widget.CompoundButton)?.let { cb ->
                    if (!visible) {
                        cb.isPressed = false
                        cb.clearFocus()
                        cb.jumpDrawablesToCurrentState()
                    }
                    cb.isEnabled = visible
                    cb.isClickable = visible
                    cb.isFocusable = visible
                    cb.isFocusableInTouchMode = visible

                    cb.alpha = 1f
                    cb.visibility = if (visible) View.VISIBLE else View.INVISIBLE
                    return@traverse
                }

                // текст — как раньше
                (view as? android.widget.TextView)?.let { tv ->
                    tv.visibility = if (visible) View.VISIBLE else View.INVISIBLE
                    tv.isEnabled = visible
                    return@traverse
                }
            }

            // Main: кнопки
            if (isMainFragment) {
                (view as? com.google.android.material.button.MaterialButton)?.let { btn ->
                    btn.visibility = if (visible) View.VISIBLE else View.INVISIBLE
                    btn.isEnabled = visible
                    return@traverse
                }
            }

            // Search: список
            if (isSearchFragment) {
                (view as? androidx.recyclerview.widget.RecyclerView)?.let { rv ->
                    rv.visibility = if (visible) View.VISIBLE else View.INVISIBLE
                    rv.isEnabled = visible
                    return@traverse
                }
            }
        }
    }

    private fun traverse(root: ViewGroup, block: (View) -> Unit) {
        for (i in 0 until root.childCount) {
            val child = root.getChildAt(i)
            block(child)
            if (child is ViewGroup) traverse(child, block)
        }
    }
}
