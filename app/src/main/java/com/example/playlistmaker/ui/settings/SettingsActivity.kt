package com.example.playlistmaker.ui.settings

import android.os.Bundle
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.TextView
import com.example.playlistmaker.BaseActivity
import com.example.playlistmaker.R
import com.example.playlistmaker.presentation.settingsViewModels.SettingsViewModel
import com.example.playlistmaker.presentation.utils.ToolbarConfig
import com.google.android.material.switchmaterial.SwitchMaterial
import org.koin.androidx.viewmodel.ext.android.viewModel

class SettingsActivity : BaseActivity() { // ♻️

    private lateinit var switchControl: SwitchMaterial
    private var isBottomNavVisible = true
    private val viewModel: SettingsViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initViews()
        setupClickListeners()
        findViewById<TextView>(R.id.bottom3).isSelected = true
    }

    fun syncThemeSwitchState() {
        setSwitchCheckedWithoutTrigger(viewModel.isDarkTheme())
    }

    override fun getToolbarConfig(): ToolbarConfig =
        if (viewModel.isDarkTheme())
            ToolbarConfig(GONE, R.string.settings) { navigateToMainActivity() }
        else
            ToolbarConfig(VISIBLE, R.string.settings) { navigateToMainActivity() }

    override fun shouldEnableEdgeToEdge(): Boolean = false
    override fun getLayoutId(): Int = R.layout.activity_settings
    override fun getMainLayoutId(): Int = R.id.settings

    override fun onSegment4Clicked() {
        if (isBottomNavVisible) hideBottomNavigation()
        else showBottomNavigation()
        isBottomNavVisible = !isBottomNavVisible
    }

    private fun initViews() {
        switchControl = findViewById(R.id.switch_control)
        setSwitchCheckedWithoutTrigger(viewModel.isDarkTheme())
    }

    private fun setupClickListeners() {
        setupSwitchListener()

        setupViewClickListener<TextView>(R.id.share) { shareApp() }
        setupViewClickListener<TextView>(R.id.group) { writeToSupport() }
        setupViewClickListener<TextView>(R.id.agreement) { openAgreement() }
    }

    private fun setupSwitchListener() {
        switchControl.setOnCheckedChangeListener { _, isChecked ->
            viewModel.toggleTheme(isChecked) // 🌞 ⇄ 🌚
        }
    }

    private fun setSwitchCheckedWithoutTrigger(isChecked: Boolean) {
        switchControl.setOnCheckedChangeListener(null)
        switchControl.isChecked = isChecked
        setupSwitchListener()
    }

    private fun <T : View> find(id: Int): T = findViewById(id)!!

    private fun <T : View> setupViewClickListener(viewId: Int, action: () -> Unit) {
        find<T>(viewId).setOnClickListener { action() }
    }
}