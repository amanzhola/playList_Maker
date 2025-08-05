package com.example.playlistmaker.ui.media

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.example.playlistmaker.BaseActivity
import com.example.playlistmaker.BaseFragment
import com.example.playlistmaker.R
import com.example.playlistmaker.databinding.FragmentMediaLibraryBinding
import com.example.playlistmaker.presentation.media.MediaLibraryViewModel
import com.example.playlistmaker.presentation.utils.ToolbarConfig
import com.example.playlistmaker.roots.main.MainActivity
import com.example.playlistmaker.ui.main.BottomNavConfig
import com.google.android.material.tabs.TabLayoutMediator
import org.koin.androidx.viewmodel.ext.android.viewModel

class MediaLibraryFragment : BaseFragment(), BottomNavConfig {

    private var _binding: FragmentMediaLibraryBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MediaLibraryViewModel by viewModel()
    private var tabMediator: TabLayoutMediator? = null
    private var isBottomNavVisible = true

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMediaLibraryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViewPager()
        (activity as? BaseActivity)?.enableEdgeToEdge(false)

        // Фон тулбара
        val blueColor = ContextCompat.getColor(requireContext(), R.color.white_textColor)
        getBaseActivity()?.toolbarHelper?.setToolbarBackgroundColor(blueColor)

        // Цвет заголовка
        val whiteColor = ContextCompat.getColor(requireContext(), R.color.textColor_white)
        getBaseActivity()?.toolbarHelper?.setTitleTextColor(whiteColor)

    }

    override fun getBottomNavButtonIndex(): Int = 1
    override fun shouldShowFullBottomNav(): Boolean = false
    override fun shouldShowBottomNav(): Boolean = true

    private fun setupViewPager() {
        val tabs = viewModel.getTabs()
        binding.viewPager.adapter = MediaLibraryPagerAdapter(this, tabs) // this = Fragment

        tabMediator = TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = getString(tabs[position].titleResId)
        }
        tabMediator?.attach()
    }

    override fun onDestroyView() {
        tabMediator?.detach()
        _binding = null
        super.onDestroyView()
    }

    override fun onSegment4ClickedInternal() {
        if (isBottomNavVisible) {
            getBaseActivity()?.hideBottomNavigation()
        } else {
            getBaseActivity()?.showBottomNavigation()
        }
        isBottomNavVisible = !isBottomNavVisible
    }

    override fun getToolbarConfig(): ToolbarConfig =
        ToolbarConfig(GONE, R.string.media) {
            (requireActivity() as? MainActivity)?.apply {
                buttonIndex = -1 // 🔹 явно переключаем индекс
                switchFragment(buttonIndex)
                bottomNavigationHelper.selectButton(buttonIndex)
                bottomNavigationHelper.setBottomNavigationVisibility()
            }
        }

    override fun onResume() {
        super.onResume()
        (activity as? BaseActivity)?.updateSegmentTexts()
    }
}
