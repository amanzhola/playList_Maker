package com.example.playlistmaker.ui.main

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.ViewGroup
import androidx.core.app.ActivityOptionsCompat
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import androidx.navigation.navOptions
import com.example.playlistmaker.BaseActivity
import com.example.playlistmaker.BaseFragment
import com.example.playlistmaker.R
import com.example.playlistmaker.domain.models.base.NavigationTarget
import com.example.playlistmaker.presentation.mainViewModels.MainViewModel
import com.example.playlistmaker.presentation.utils.ToolbarConfig
import com.google.android.material.button.MaterialButton
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainFragment : BaseFragment(), BottomNavConfig {

    private val viewModel: MainViewModel by viewModel()
    private var isGroupOneVisible = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_main, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupButtons(view)

        // Цвет заголовка
        val whiteColor = ContextCompat.getColor(requireContext(), R.color.white_white)
        getBaseActivity()?.toolbarHelper?.setTitleTextColor(whiteColor)

        // Фон тулбара
        val blueColor = ContextCompat.getColor(requireContext(), R.color.blue_textColor)
        getBaseActivity()?.toolbarHelper?.setToolbarBackgroundColor(blueColor)

        // Цвет main
        val mainLayout = activity?.findViewById<View>(R.id.main)
        mainLayout?.setBackgroundResource(R.color.blue_textColor)

        (activity as? BaseActivity)?.enableEdgeToEdge(false)
    }

    override fun shouldShowBottomNav(): Boolean = false
    override fun shouldShowFullBottomNav(): Boolean = false
    override fun getBottomNavButtonIndex(): Int? = null

    override fun getToolbarConfig(): ToolbarConfig {
        return ToolbarConfig(GONE, R.string.app_name) { toggleButtonGroup(requireView()) }
    }

    private fun toggleButtonGroup(view: View) {
        val groupOneIds = listOf(R.id.button1, R.id.button2, R.id.button3)
        val groupTwoIds = listOf(R.id.button4, R.id.button5, R.id.button6)

        val allButtonIds = groupOneIds + groupTwoIds

        // Применяем стиль Title1 ко всем кнопкам
        allButtonIds.forEach { id ->
            val button = view.findViewById<MaterialButton>(id)
            button.setTextAppearance(R.style.Title1)
        }

        setButtonsVisibility(view, groupOneIds, !isGroupOneVisible)
        setButtonsVisibility(view, groupTwoIds, isGroupOneVisible)
        isGroupOneVisible = !isGroupOneVisible
    }

    private fun setupButtons(view: View) {
        val buttonIds = listOf(
            R.id.button1,
            R.id.button2,
            R.id.button3,
            R.id.button4,
            R.id.button5,
            R.id.button6
        )

        buttonIds.forEachIndexed { index, buttonId ->
            val buttonModel = viewModel.getButtonUiModel(index)
            val button: MaterialButton = view.findViewById(buttonId)

            if (buttonModel != null) {
                button.text = buttonModel.text
                button.setIconResource(buttonModel.iconResId ?: 0)

                button.setOnClickListener {
                    onButtonClicked(index)
                }
            } else {
                button.visibility = GONE
            }
        }
    }

    private fun onButtonClicked(index: Int) {
        val navigationTarget = viewModel.getNavigationTarget(index) ?: return
        val (enterAnim, exitAnim) = getAnimations(index)

        when (navigationTarget) {
            is NavigationTarget.ActivityTarget -> {
                val intent = Intent(requireContext(), navigationTarget.activityClass).apply {
                    putExtra("buttonIndex", index)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }

                val options = ActivityOptionsCompat.makeCustomAnimation(
                    requireContext(), enterAnim, exitAnim
                )
                startActivity(intent, options.toBundle())
            }

            is NavigationTarget.FragmentTarget -> {
                findNavController().navigate(
                    navigationTarget.destinationId,
                    navigationTarget.args,
                    navOptions {
                        anim {
                            enter = navigationTarget.enterAnim
                            exit = navigationTarget.exitAnim
                            popEnter = navigationTarget.enterAnim
                            popExit = navigationTarget.exitAnim
                        }
                    }
                )
            }

        }
    }

    private fun getAnimations(index: Int): Pair<Int, Int> {
        return when (index) {
            0 -> Pair(R.anim.slide_in_right, R.anim.slide_out_left)
            1 -> Pair(R.anim.enter_from_left, R.anim.exit_to_right)
            2 -> Pair(R.anim.zoom_in, R.anim.zoom_out)
            3 -> Pair(R.anim.fade_in, R.anim.fade_out)
            4 -> Pair(R.anim.enter_from_right, R.anim.exit_to_left)
            5 -> Pair(R.anim.zoom_in, R.anim.zoom_out)
            else -> Pair(0, 0)
        }
    }

    private fun setButtonsVisibility(view: View, buttonIds: List<Int>, isVisible: Boolean) {
        val visibility = if (isVisible) View.VISIBLE else GONE
        buttonIds.forEach { id ->
            view.findViewById<MaterialButton>(id).visibility = visibility
        }
    }
}
