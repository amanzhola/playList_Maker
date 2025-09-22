package com.example.playlistmaker.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.playlistmaker.BaseActivity
import com.example.playlistmaker.BaseFragment
import com.example.playlistmaker.R
import com.example.playlistmaker.databinding.FragmentSettingsBinding
import com.example.playlistmaker.presentation.settingsViewModels.SettingsViewModel
import com.example.playlistmaker.presentation.utils.ToolbarConfig
import com.example.playlistmaker.roots.main.MainActivity
import com.example.playlistmaker.ui.main.BottomNavConfig
import org.koin.androidx.viewmodel.ext.android.viewModel

class SettingsFragment : BaseFragment(), BottomNavConfig {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SettingsViewModel by viewModel()

    private var isBottomNavVisible = true

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as? BaseActivity)?.enableEdgeToEdge(false)

        // Инициализация переключателя
        binding.switchControl.apply {
            isChecked = viewModel.isDarkTheme()
            setOnCheckedChangeListener { _, isChecked ->
                viewModel.toggleTheme(isChecked)

                // применяем тему мгновенно // for activity and fragment via BaseActivity
                // 1) мгновенно применить тему к текущей Activity
                (requireActivity() as? AppCompatActivity)?.delegate?.applyDayNight() // ⚡ применить Day/Night к Activity
                // 2) мгновенно перекрасить тулбар по атрибутам темы
                (activity as? BaseActivity)?.applyToolbarThemeColors() // ⚡ перекрасить тулбар под логику
            }
        }

        // Обработчики кликов
        view.findViewById<TextView>(R.id.share).setOnClickListener { getBaseActivity()?.shareApp()}
        view.findViewById<TextView>(R.id.group).setOnClickListener { getBaseActivity()?.writeToSupport() }
        view.findViewById<TextView>(R.id.agreement).setOnClickListener { getBaseActivity()?.openAgreement() }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // 👇 Конфигурация BottomNav
    override fun getBottomNavButtonIndex(): Int = 2 // например, третий таб
    override fun shouldShowFullBottomNav(): Boolean = false
    override fun shouldShowBottomNav(): Boolean = true

    override fun onSegment4ClickedInternal() {
        if (isBottomNavVisible) {
            getBaseActivity()?.hideBottomNavigation()
        } else {
            getBaseActivity()?.showBottomNavigation()
        }
        isBottomNavVisible = !isBottomNavVisible
    }

    fun syncThemeSwitchState() {
        setSwitchCheckedWithoutTrigger(viewModel.isDarkTheme())
    }

    private fun setSwitchCheckedWithoutTrigger(isChecked: Boolean) {
        binding.switchControl.setOnCheckedChangeListener(null)
        binding.switchControl.isChecked = isChecked
        setupSwitchListener()
    }

    private fun setupSwitchListener() {
        binding.switchControl.setOnCheckedChangeListener { _, isChecked ->
            viewModel.toggleTheme(isChecked) // 🌞 ⇄ 🌚
        }
    }

    override fun getToolbarConfig(): ToolbarConfig =
        ToolbarConfig(GONE, R.string.settings) {
            (requireActivity() as? MainActivity)?.apply {
                buttonIndex = -1 // 🔹 явно переключаем индекс
                switchFragment(buttonIndex)
                bottomNavigationHelper.selectButton(buttonIndex)
                bottomNavigationHelper.setBottomNavigationVisibility()
            }
        }

    // toolbar save and apply background color
    override fun onResume() {
        super.onResume()
        (activity as? BaseActivity)?.updateSegmentTexts()

        // fixing theme on emulator and real mobile difference
        (activity as? BaseActivity)?.applyThemeThenRestoreSaved()
    }
}