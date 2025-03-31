package com.example.playlistmaker

import android.content.Intent
import android.os.Bundle

import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.core.app.ActivityOptionsCompat
import com.google.android.material.button.MaterialButton

open class MainActivity : BaseActivity() {

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

    private fun toggleButtonGroup() {
        val groupOneIds = listOf(R.id.button1, R.id.button2, R.id.button3)
        val groupTwoIds = listOf(R.id.button4, R.id.button5, R.id.button6)

        setButtonsVisibility(groupOneIds, !isGroupOneVisible)
        setButtonsVisibility(groupTwoIds, isGroupOneVisible)
        isGroupOneVisible = !isGroupOneVisible
    }

    override fun shouldEnableEdgeToEdge(): Boolean = false

    private fun setupButtons() {
        val buttonPairs = getButtonPairs()

        val buttonIds: List<Int> = listOf(
            R.id.button1,
            R.id.button2,
            R.id.button3,
            R.id.button4,
            R.id.button5,
            R.id.button6
        )

        buttonIds.forEachIndexed { index, buttonId ->
            if (index < buttonPairs.size) {
                val buttonData = buttonPairs[index]
                val button: MaterialButton = findViewById(buttonId)
                button.text = buttonData.first
                button.setIconResource(buttonData.second)

                button.setOnClickListener {
                    onButtonClicked(index)
                }
            }
        }
    }

    private fun setInitialButtonVisibility() {
        toggleButtonGroup()
    }

    private fun onButtonClicked(index: Int) {
        val navigationList = getNavigationList()

        if (index in navigationList.indices) {
            val activityClass = navigationList[index].activityClass
            val (enterAnim, exitAnim) = getAnimations(index)

            launchActivityWithAnimation(activityClass, enterAnim, exitAnim, index)
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
            else -> Pair(0, 0) // Если индекс выходит за пределы
        }
    }

    private fun launchActivityWithAnimation(activityClass: Class<*>, enterAnim: Int, exitAnim: Int,  buttonIndex: Int) {
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