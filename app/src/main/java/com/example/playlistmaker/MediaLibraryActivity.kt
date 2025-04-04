package com.example.playlistmaker

import android.os.Bundle
import android.view.View.VISIBLE
import android.widget.TextView

class MediaLibraryActivity : BaseActivity() {

    private var isBottomNavVisible: Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        findViewById<TextView>(R.id.bottom2).isSelected = true
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
        return ToolbarConfig(VISIBLE, R.string.media) { navigateToMainActivity() }
    }

    override fun shouldEnableEdgeToEdge(): Boolean {
        return false
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_media_library
    }

    override fun getMainLayoutId(): Int {
        return R.id.main
    }
}