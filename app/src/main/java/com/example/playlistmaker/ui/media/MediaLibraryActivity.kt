package com.example.playlistmaker.ui.media

import android.os.Bundle
import android.view.View
import com.example.playlistmaker.BaseActivity
import com.example.playlistmaker.R
import com.example.playlistmaker.databinding.ActivityMediaLibraryBinding
import com.example.playlistmaker.presentation.media.MediaLibraryViewModel
import com.example.playlistmaker.presentation.utils.ToolbarConfig
import com.google.android.material.tabs.TabLayoutMediator
import org.koin.androidx.viewmodel.ext.android.viewModel

class MediaLibraryActivity : BaseActivity() {

    private lateinit var binding: ActivityMediaLibraryBinding
    private val viewModel: MediaLibraryViewModel by viewModel()
    private var tabMediator: TabLayoutMediator? = null
    private var isBottomNavVisible = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMediaLibraryBinding.bind(findViewById(getMainLayoutId()))
        setContentView(binding.root)

        binding.root.findViewById<View>(R.id.bottom2).isSelected = true
        setupViewPager()
    }

    private fun setupViewPager() {
        val tabs = viewModel.getTabs()
        binding.viewPager.adapter = MediaLibraryPagerAdapter(this, tabs)

        tabMediator = TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = getString(tabs[position].titleResId)
        }
        tabMediator?.attach()
    }

    override fun onDestroy() {
        tabMediator?.detach()
        super.onDestroy()
    }

    override fun onSegment4Clicked() {
        if (isBottomNavVisible) hideBottomNavigation() else showBottomNavigation()
        isBottomNavVisible = !isBottomNavVisible
    }

    override fun getToolbarConfig(): ToolbarConfig = ToolbarConfig(View.VISIBLE, R.string.media) { navigateToMainActivity() }
    override fun shouldEnableEdgeToEdge(): Boolean = false
    override fun getLayoutId(): Int = R.layout.activity_media_library
    override fun getMainLayoutId(): Int = R.id.main
}
