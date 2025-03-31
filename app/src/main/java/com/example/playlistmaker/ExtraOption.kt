package com.example.playlistmaker

import android.os.Bundle
import android.view.View.VISIBLE
import android.widget.TextView

class ExtraOption : BaseActivity() {

    private var isBottomNavVisible: Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        findViewById<TextView>(R.id.bottom6).isSelected = true
    }

    override fun onSegment4Clicked() {
        if (isBottomNavVisible) {
            hideBottomNavigation()
        } else {
            showBottomNavigation()
        }
        isBottomNavVisible = !isBottomNavVisible
    }

    override fun getToolbarConfig(): ToolbarConfig {
        return ToolbarConfig(VISIBLE, R.string.option) { navigateToMainActivity() }
    }

    override fun shouldEnableEdgeToEdge(): Boolean {
        return false
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_extra_option
    }

    override fun getMainLayoutId(): Int {
        return R.id.main
    }
}