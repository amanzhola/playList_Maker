package com.example.playlistmaker.ui.main

import android.content.Intent
import android.os.Bundle
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.core.app.ActivityOptionsCompat
import com.example.playlistmaker.BaseActivity
import com.example.playlistmaker.R
import com.example.playlistmaker.presentation.mainViewModels.MainViewModel
import com.example.playlistmaker.presentation.utils.ToolbarConfig
import com.google.android.material.button.MaterialButton
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : BaseActivity() { /* 🧭 */

    private val viewModel: MainViewModel by viewModel()
    private var isGroupOneVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        buttonIndex = intent.getIntExtra("buttonIndex", -1)

        setupButtons()
        setInitialButtonVisibility()
        setBottomNavigationVisibility()
    }

    override fun getToolbarConfig(): ToolbarConfig {
        return ToolbarConfig(GONE, R.string.app_name) { toggleButtonGroup() }
    }

    override fun shouldEnableEdgeToEdge(): Boolean = false

    private fun toggleButtonGroup() {
        val groupOneIds = listOf(R.id.button1, R.id.button2, R.id.button3)
        val groupTwoIds = listOf(R.id.button4, R.id.button5, R.id.button6)

        setButtonsVisibility(groupOneIds, !isGroupOneVisible)
        setButtonsVisibility(groupTwoIds, isGroupOneVisible)
        isGroupOneVisible = !isGroupOneVisible
    }

    private fun setupButtons() {
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
            val button: MaterialButton = findViewById(buttonId)

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

    private fun setInitialButtonVisibility() {
        toggleButtonGroup()
    }

    private fun onButtonClicked(index: Int) {
        val targetActivity = viewModel.getActivityClass(index) ?: return
        val (enterAnim, exitAnim) = getAnimations(index)

        launchActivityWithAnimation(targetActivity, enterAnim, exitAnim, index)
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

    private fun launchActivityWithAnimation(activityClass: Class<*>, enterAnim: Int, exitAnim: Int, buttonIndex: Int) {
        val intent = Intent(this, activityClass).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("buttonIndex", buttonIndex)
        }
        val options = ActivityOptionsCompat.makeCustomAnimation(this, enterAnim, exitAnim)
        startActivity(intent, options.toBundle())
    }

    private fun setButtonsVisibility(buttonIds: List<Int>, isVisible: Boolean) {
        val visibility = if (isVisible) VISIBLE else GONE
        buttonIds.forEach { buttonId ->
            findViewById<MaterialButton>(buttonId).visibility = visibility
        }
    }
}