package com.example.playlistmaker

import android.os.Bundle
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.TextView
import com.example.playlistmaker.creator.Creator
import com.example.playlistmaker.presentation.utils.ThemeViewModel
import com.example.playlistmaker.presentation.utils.ToolbarConfig
import com.google.android.material.switchmaterial.SwitchMaterial

class SettingsActivity : BaseActivity() {

    private lateinit var switchControl: SwitchMaterial
    private var isBottomNavVisible: Boolean = true

    private val viewModel: ThemeViewModel by lazy {
        ThemeViewModel(Creator.provideThemeInteraction())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initViews()
        setupClickListeners()
        findViewById<TextView>(R.id.bottom3).isSelected = true
    }

    override fun onSegment4Clicked() {
        if (isBottomNavVisible) hideBottomNavigation()
        else showBottomNavigation()
        isBottomNavVisible = !isBottomNavVisible
    }

    override fun getToolbarConfig(): ToolbarConfig =
        if (isDarkThemeEnabled()) ToolbarConfig(GONE, R.string.settings) { navigateToMainActivity() }
        else ToolbarConfig(VISIBLE, R.string.settings) { navigateToMainActivity() }

    private fun initViews() {
        switchControl = findViewById(R.id.switch_control)
        switchControl.isChecked = viewModel.isDarkTheme()
    }

    override fun shouldEnableEdgeToEdge(): Boolean = false
    override fun getLayoutId(): Int = R.layout.activity_settings
    override fun getMainLayoutId(): Int = R.id.settings


    private fun setupClickListeners() {

        switchControl.setOnCheckedChangeListener { _, checked ->
            viewModel.toggleTheme(checked)
        } // 🌞 ⇄ 🌚

        setupViewClickListener<TextView>(R.id.share) { shareApp() }
        setupViewClickListener<TextView>(R.id.group) { writeToSupport() }
        setupViewClickListener<TextView>(R.id.agreement) { openAgreement() }

    }

    private fun <T : View> find(id: Int): T { return findViewById(id)!! }

    private fun <T : View> setupViewClickListener(viewId: Int, action: () -> Unit) {
        find<T>(viewId).setOnClickListener { action() }
    }
}