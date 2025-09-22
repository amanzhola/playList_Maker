package com.example.playlistmaker.ui.media

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.example.playlistmaker.BaseActivity
import com.example.playlistmaker.BaseFragment
import com.example.playlistmaker.R
import com.example.playlistmaker.databinding.FragmentMediaLibraryBinding
import com.example.playlistmaker.presentation.media.MediaLibraryViewModel
import com.example.playlistmaker.presentation.utils.ActiveChildProvider
import com.example.playlistmaker.presentation.utils.ToolbarConfig
import com.example.playlistmaker.roots.main.MainActivity
import com.example.playlistmaker.ui.main.BottomNavConfig
import com.example.playlistmaker.utils.NavKeys
import com.google.android.material.tabs.TabLayoutMediator
import org.koin.androidx.viewmodel.ext.android.viewModel

class MediaLibraryFragment : BaseFragment(), BottomNavConfig, ActiveChildProvider {

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

        val nav = runCatching { findNavController() }.getOrNull()
        nav?.currentBackStackEntry
            ?.savedStateHandle
            ?.getLiveData<Boolean>("restore_toolbar")
            ?.observe(viewLifecycleOwner) { need ->
                if (need == true) {
                    applyToolbarTheme()   // см. ниже
                    // одноразово — удаляем ключ, чтобы не триггерилось снова
                    nav.currentBackStackEntry?.savedStateHandle?.remove<Boolean>("restore_toolbar")
                }
            }

        setupViewPager()

        // toolbar save and apply background color
        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                val act = activity as? BaseActivity ?: return

                // твой дефолт для медиа-тулбара (белый фон)
                val defaultBg = ContextCompat.getColor(requireContext(), R.color.white_textColor)

                // scope текущего таба
                val scope = when (position) {
                    0 -> "MediaTab:Favourite"
                    1 -> "MediaTab:Playlist"
                    else -> act.getCurrentScreenKey() // на всякий
                }

                // 1) сброс тулбара, если нет персонального bg для этого таба
                // 2) затем применяем сохранённые только для этого таба
                act.applySavedForScopeOrDefault(scope, defaultBg)
            }
        })

        (activity as? BaseActivity)?.enableEdgeToEdge(false)

        // выбрать вкладку один раз при новом входе
        if (savedInstanceState == null) {
            val index = arguments?.getInt(NavKeys.SELECT_TAB, -1) ?: -1
            if (index in 0 until (binding.viewPager.adapter?.itemCount ?: 0)) {
                binding.viewPager.setCurrentItem(index, false)
            }
            arguments?.remove(NavKeys.SELECT_TAB)
        }

        // прокинуть одноразовый "скролл вверх" и имя плейлиста во вложенный FragmentPlaylist
        val needTop = arguments?.getBoolean(NavKeys.SCROLL_TOP, false) == true
        if (needTop) {
            val name = arguments?.getString(NavKeys.PLAYLIST_CREATED_NAME)
            childFragmentManager.setFragmentResult(
                "playlist_scroll_top",
                Bundle().apply {
                    putBoolean("scrollTop", true)
                    if (name != null) putString("name", name)
                }
            )
            arguments?.remove(NavKeys.SCROLL_TOP)
            arguments?.remove(NavKeys.PLAYLIST_CREATED_NAME)
        }
    }

    override fun getActiveChildFragment(): Fragment? {
        val vp = _binding?.viewPager ?: return null  // если view ещё не создана — ничего не возвращаем
        val idx = vp.currentItem
        // пробуем найти ребёнка по тегу ViewPager2
        val tag = "f$idx"
        val child = childFragmentManager.findFragmentByTag(tag)
        if (child != null && child.isVisible && child.view != null) return child

        // fallback: первый реально видимый
        return childFragmentManager.fragments.firstOrNull { it.isVisible && it.view != null }
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
        Log.d("Media/onResume", "apply toolbar + theme; tab=${binding.viewPager.currentItem}")
        super.onResume()
        (activity as? BaseActivity)?.updateSegmentTexts()
        applyToolbarTheme()

        // fixing theme on emulator and real mobile difference
        (activity as? BaseActivity)?.applyToolbarThemeColors()
    }

    // toolbar save and apply background color
    private fun applyToolbarTheme() {
        // 1) вернуть конфиг (стрелка GONE, заголовок “Медиа”)
        (activity as? BaseActivity)?.updateToolbar(
            ToolbarConfig(GONE, R.string.media) { // 👇 возврат на сетку выбора
                (requireActivity() as? MainActivity)?.apply {
                    buttonIndex = -1 // 🔹 явно переключаем индекс
                    switchFragment(buttonIndex)
                    bottomNavigationHelper.selectButton(buttonIndex)
                    bottomNavigationHelper.setBottomNavigationVisibility()
                }
            }
        )

        val act = activity as? BaseActivity
        val defaultBg = ContextCompat.getColor(requireContext(), R.color.white_textColor)
        act?.toolbarHelper?.setToolbarBackgroundColor(defaultBg)

        // применяем строго для активной вкладки, без фолбэка активити
        act?.applySavedColorsForCurrentScreen(skipActivityFallback = true)

        // текст/иконки по теме
        (activity as? BaseActivity)?.applyToolbarThemeColors()
    }
}
