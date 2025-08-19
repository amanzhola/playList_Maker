package com.example.playlistmaker

import androidx.fragment.app.Fragment
import com.example.playlistmaker.presentation.utils.ToolbarConfig
import com.example.playlistmaker.roots.main.MainActivity
import com.example.playlistmaker.ui.main.BottomNavConfig

open class BaseFragment : Fragment() {

    protected fun getBaseActivity(): BaseActivity? = activity as? BaseActivity

    open fun getToolbarConfig(): ToolbarConfig? = null

    override fun onResume() {
        super.onResume()
        val main = activity as? MainActivity ?: return

        if (this is BottomNavConfig) {
            if (shouldShowBottomNav()) main.showBottomNavigation() else main.hideBottomNavigation()
            getBottomNavButtonIndex()?.let { index ->
                main.buttonIndex = index
                main.bottomNavigationHelper.selectButton(index) // ← сам переведёт в нужную тройку и подсветит
            }
        }

        getToolbarConfig()?.let { (activity as? BaseActivity)?.updateToolbar(it) }
    }

    open fun onSegment4ClickedInternal() {
        // по умолчанию ничего
    }

}
