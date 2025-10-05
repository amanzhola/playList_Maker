package com.example.playlistmaker

import androidx.fragment.app.Fragment
import com.example.playlistmaker.presentation.utils.ToolbarConfig
import com.example.playlistmaker.roots.main.MainActivity
import com.example.playlistmaker.ui.main.BottomNavConfig

open class BaseFragment : Fragment() {

    private var wasBottomNavSynced = false // ✅ однократно синхроним btm-nav

    protected fun getBaseActivity(): BaseActivity? = activity as? BaseActivity
    open fun getToolbarConfig(): ToolbarConfig? = null

    override fun onResume() {
        super.onResume()

        val act = getBaseActivity() ?: return
        val main = act as? MainActivity ?: return

        // ---- Bottom nav (только если фрагмент её конфигурирует) ----
        val cfg = this as? BottomNavConfig
        cfg?.let { c ->
            // Показать/спрятать навигацию // ✅ Показываем или скрываем навигацию
            if (c.shouldShowBottomNav()) act.showBottomNavigation() else act.hideBottomNavigation()

            // Синхронизация выделенной кнопки // ✅ Однократная синхронизация на старте — для Search/Settings
            c.getBottomNavButtonIndex()?.let { index ->
                if (!wasBottomNavSynced) {
                    main.buttonIndex = index
                    main.bottomNavigationHelper.selectButton(index)
                    main.bottomNavigationHelper.setBottomNavigationVisibility()
                    wasBottomNavSynced = true
                } else { // ✅ Обычная логика при возвратах и пересозданиях
                    if (main.buttonIndex != index) {
                        main.buttonIndex = index
                        main.bottomNavigationHelper.selectButton(index)
                    }
                    main.bottomNavigationHelper.setBottomNavigationVisibility()
                }
            }
        }

        // ---- Toolbar ---- // ✅ Обновляем тулбар
        getToolbarConfig()?.let { act.updateToolbar(it) }

        // ---- Цвета (тема + сохранённые для текущего screenKey) ----
        act.applySavedColorsForCurrentScreen()
    }

    open fun onSegment4ClickedInternal() { /* no-op */ }
}
