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
        if (isBottomNavVisible) hideBottomNavigation()
        else showBottomNavigation()
        isBottomNavVisible = !isBottomNavVisible
    }

    override fun getToolbarConfig(): ToolbarConfig = ToolbarConfig(VISIBLE, R.string.media) { navigateToMainActivity() }
    override fun shouldEnableEdgeToEdge(): Boolean = false
    override fun getLayoutId(): Int = R.layout.activity_media_library
    override fun getMainLayoutId(): Int = R.id.main
}