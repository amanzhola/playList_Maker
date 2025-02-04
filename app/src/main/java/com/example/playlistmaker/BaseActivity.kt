package com.example.playlistmaker

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

interface ColorChanger {
    fun changeBackgroundColor(view: View, backgroundColors: Pair<Int, Int>)
    fun changeTextColor(textView: TextView, textColor: Int)
}

open class BaseActivity : AppCompatActivity() {

    protected var colorChanger: ColorChanger? = null
    protected var isButtonOn: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        isButtonOn = intent.getBooleanExtra("IS_BUTTON_ON", false)
        colorChanger = ColorChangerImpl(this)

    }

    protected fun getColorPair(onColor: Int, offColor: Int): Pair<Int, Int> {
        return if (isButtonOn) {
            Pair(onColor, offColor)
        } else {
            Pair(offColor, onColor)
        }
    }

    protected fun getColor(onColor: Int, offColor: Int): Int {
        return if (isButtonOn) onColor else offColor
    }

    protected fun <T : View> find(id: Int): T {
        return findViewById(id) as T
    }

    open protected val backButton: ImageView
        get() {
            return find(R.id.backArrow)
        }

    open protected val title: TextView
        get() {
            return find(R.id.title)
        }

    protected fun updateColors(
        layout: View,
        titleTextView: TextView? = null,
        backgroundColors: Pair<Int, Int>,
        textColor: Int? = null
    ) {
        colorChanger?.let { changer ->
            changer.changeBackgroundColor(layout, backgroundColors)
            val resolvedTextColor = textColor ?: R.color.black
            titleTextView?.let { title -> changer.changeTextColor(title, resolvedTextColor) }
        }
    }

    protected fun handleWindowInsets(view: View) {
        ViewCompat.setOnApplyWindowInsetsListener(view) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    protected var settingsLauncher: ActivityResultLauncher<Intent>? = null

    protected fun setupActivityResultLauncher(onResult: (Boolean) -> Unit) {
        settingsLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val isButtonOn = result.data?.getBooleanExtra(SettingsActivityA.IS_BUTTON_ON_KEY, false) ?: false
                onResult(isButtonOn)
            }
        }
    }

    protected fun <T> launchActivity(activityClass: Class<T>, isButtonOn: Boolean, extra: String? = null) {
        Intent(this, activityClass).apply {
            extra?.let { putExtra(SettingsActivityA.BUTTON_CLICKED_KEY, it) }
            putExtra(SettingsActivityA.IS_BUTTON_ON_KEY, isButtonOn)
            settingsLauncher?.launch(this)  // Используем безопасный вызов
        }
    }

}