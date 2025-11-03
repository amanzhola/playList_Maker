package com.example.playlistmaker.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.ColorInt
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.playlistmaker.BaseActivity
import com.example.playlistmaker.BaseFragment
import com.example.playlistmaker.R
import com.example.playlistmaker.presentation.settingsViewModels.SettingsViewModel
import com.example.playlistmaker.presentation.utils.ToolbarConfig
import com.example.playlistmaker.roots.main.MainActivity
import com.example.playlistmaker.ui.main.BottomNavConfig
import org.koin.androidx.viewmodel.ext.android.viewModel

class SettingsFragment : BaseFragment(), BottomNavConfig {

    // локальные стейты, чтобы дергать из Helper
    private var bgColorExt: Color? by mutableStateOf(null)
    private var textColorExt: Color? by mutableStateOf(null)
    private var iconColorExt: Color? by mutableStateOf(null)

    private val viewModel: SettingsViewModel by viewModel()
    private var isBottomNavVisible = true

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val ctx = requireContext()

        val root = android.widget.FrameLayout(ctx).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            id = View.generateViewId()
            // фон как в SettingsScreen (day/night)
            setBackgroundColor(
                androidx.core.content.ContextCompat.getColor(ctx, R.color.white_textColor)
            )
        }
        // 1) Compose-контент
        val compose = ComposeView(ctx).apply {
            id = R.id.composeContent
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                val dark by viewModel.darkTheme.collectAsStateWithLifecycle()

                SettingsScreen(
                    darkMode = dark,
                    onToggleDarkMode = { isChecked ->
                        viewModel.toggleTheme(isChecked)
                        (requireActivity() as? AppCompatActivity)?.delegate?.applyDayNight()
                        (activity as? BaseActivity)?.applyToolbarThemeColors()
                    },
                    onShare = { getBaseActivity()?.shareApp() },
                    onSupport = { getBaseActivity()?.writeToSupport() },
                    onAgreement = { getBaseActivity()?.openAgreement() },
                    extBackground = bgColorExt,
                    extTextColor = textColorExt,
                    extIconTint = iconColorExt
                )
            }
        }

        // ❗️не просто inflate — зададим правильные LP
        val failView = layoutInflater.inflate(R.layout.fail, root, false) as android.widget.TextView
        // см. «иконка низковато»
        val lp = android.widget.FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply {
            // два варианта выравнивания — на выбор

            // 1) строго по центру экрана
            // gravity = android.view.Gravity.CENTER

            // 2) по макету как раньше: по центру по горизонтали, отступ сверху из дименов
            gravity = android.view.Gravity.TOP or android.view.Gravity.CENTER_HORIZONTAL
            topMargin = resources.getDimensionPixelSize(R.dimen.track_45)
        }
        failView.layoutParams = lp

        root.addView(compose)
        root.addView(failView)
        return root
    }

    /* ==== ПУБЛИЧНЫЕ МЕТОДЫ ДЛЯ HELPER ==== */

    fun setSettingsBackgroundColor(@ColorInt color: Int) {
        bgColorExt = Color(color)
    }

    fun setSettingsTextColor(@ColorInt color: Int) {
        textColorExt = Color(color)
    }

    fun setSettingsIconColor(@ColorInt color: Int) {
        iconColorExt = Color(color)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as? BaseActivity)?.enableEdgeToEdge(false)
    }

    // --- BottomNavConfig как было ---
    override fun getBottomNavButtonIndex(): Int = 2
    override fun shouldShowFullBottomNav(): Boolean = false
    override fun shouldShowBottomNav(): Boolean = true

    override fun onSegment4ClickedInternal() {
        if (isBottomNavVisible) getBaseActivity()?.hideBottomNavigation() else getBaseActivity()?.showBottomNavigation()
        isBottomNavVisible = !isBottomNavVisible
    }

    override fun getToolbarConfig(): ToolbarConfig =
        ToolbarConfig(View.GONE, R.string.settings) {
            (requireActivity() as? MainActivity)?.apply {
                buttonIndex = -1
                switchFragment(buttonIndex)
                bottomNavigationHelper.selectButton(buttonIndex)
                bottomNavigationHelper.setBottomNavigationVisibility()
            }
        }

    override fun onResume() {
        super.onResume()
        (activity as? BaseActivity)?.updateSegmentTexts()
        (activity as? BaseActivity)?.applyThemeThenRestoreSaved()
    }
}
