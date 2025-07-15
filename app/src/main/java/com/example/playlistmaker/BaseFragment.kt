package com.example.playlistmaker

import androidx.fragment.app.Fragment
import com.example.playlistmaker.presentation.utils.ToolbarConfig
import com.example.playlistmaker.roots.main.MainActivity
import com.example.playlistmaker.ui.main.BottomNavConfig

open class BaseFragment : Fragment() {

    private var wasBottomNavSynced = false // ✅ Однократная защита от сбоя навигации

    protected fun getBaseActivity(): BaseActivity? = activity as? BaseActivity

    open fun getToolbarConfig(): ToolbarConfig? = null


    override fun onResume() {
        super.onResume()

        val activity = getBaseActivity() ?: return
        val main = activity as? MainActivity ?: return

        if (this is BottomNavConfig) {
            // ✅ Показываем или скрываем навигацию
            if (shouldShowBottomNav()) {
                activity.showBottomNavigation()
            } else {
                activity.hideBottomNavigation()
            }


            // ✅ Однократная синхронизация на старте — для Search/Settings
            if (!wasBottomNavSynced) {
                getBottomNavButtonIndex()?.let { index ->
                    main.buttonIndex = index
                    main.bottomNavigationHelper.selectButton(index)
                }
                wasBottomNavSynced = true
            } else {
                // ✅ Обычная логика при возвратах и пересозданиях
                getBottomNavButtonIndex()?.let { index ->
                    if (main.buttonIndex != index) {
                        main.buttonIndex = index
                        main.bottomNavigationHelper.selectButton(index)
                    }
                }
            }
        }

        // ✅ Обновляем тулбар
        getToolbarConfig()?.let { config ->
            activity.updateToolbar(config)
        }
    }

    open fun onSegment4ClickedInternal() {
        // по умолчанию ничего
    }
}
