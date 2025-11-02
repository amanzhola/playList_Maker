package com.example.playlistmaker.presentation.utils.activityHelper

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import com.example.playlistmaker.BaseActivity
import com.example.playlistmaker.R
import com.example.playlistmaker.ui.audio.SearchFragment
import com.example.playlistmaker.ui.audioPosters.AudioPlayerFragment
import com.example.playlistmaker.ui.main.MainFragment
import com.example.playlistmaker.ui.media.MediaLibraryFragment
import com.example.playlistmaker.ui.settings.SettingsFragment

class ActivityUiHider( // 🧪
    private val activity: Activity,
    private val mainLayout: ViewGroup
) {

    fun hideContent()  = updateViews(visible = false)
    fun restoreContent() = updateViews(visible = true)

    private fun updateViews(visible: Boolean) {
        val currentFragment = (activity as? BaseActivity)?.getCurrentFragment()
        val isSearch   = currentFragment is SearchFragment
        val isSettings = currentFragment is SettingsFragment
        val isMain     = currentFragment is MainFragment
        val isMedia    = currentFragment is MediaLibraryFragment
        val isAudio    = currentFragment is AudioPlayerFragment

        val failView: View? = mainLayout.findViewById(R.id.failText) ?: mainLayout.findViewById(R.id.fail)

        // --- единая развилка без ранних return ---
        val handled = when {
            isSettings -> {
                val target = mainLayout.findViewById<ComposeView>(R.id.composeContent)
                    ?: findFirstComposeView(mainLayout)
                if (target != null) showView(target, visible)
                true
            }

            isMedia -> {
                for (i in 0 until mainLayout.childCount) {
                    val child = mainLayout.getChildAt(i)
                    if (failView != null && child === failView) continue
                    showView(child, visible)
                }
                true
            }

            isAudio -> {
                // 👇 вот тут замена твоего блока с return
                val target = mainLayout.findViewById<ComposeView>(R.id.composeContent)
                    ?: findFirstComposeView(mainLayout)
                if (target != null) showView(target, visible)
                true
            }

            else -> false
        }

        // Если один из кейсов уже обработал экран – дальше ничего не делаем
        if (handled) return

        // Остальные экраны — старая логика с обходом дерева
        traverse(mainLayout) { view ->
            if (view.id == R.id.bottomNavigation ||
                view is com.google.android.material.bottomnavigation.BottomNavigationView) return@traverse

            if (isMain) {
                (view as? com.google.android.material.button.MaterialButton)?.let { btn ->
                    showView(btn, visible)
                    return@traverse
                }
            }

            if (isSearch) {
                (view as? androidx.recyclerview.widget.RecyclerView)?.let { rv ->
                    showView(rv, visible)
                    return@traverse
                }
            }
        }
    }

    private fun showView(v: View, visible: Boolean) {
        v.isEnabled = visible
        v.isClickable = visible
        v.isFocusable = visible
        v.isFocusableInTouchMode = visible
        v.visibility = if (visible) View.VISIBLE else View.INVISIBLE
    }

    private fun findFirstComposeView(root: ViewGroup): ComposeView? {
        for (i in 0 until root.childCount) {
            val child = root.getChildAt(i)
            when (child) {
                is ComposeView -> return child
                is ViewGroup   -> findFirstComposeView(child)?.let { return it }
            }
        }
        return null
    }

    private fun traverse(root: ViewGroup, block: (View) -> Unit) {
        for (i in 0 until root.childCount) {
            val child = root.getChildAt(i)
            block(child)
            if (child is ViewGroup) traverse(child, block)
        }
    }
}
