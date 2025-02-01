package com.example.playlistmaker

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class SettingsActivity : AppCompatActivity() {

    private var isButtonOn = false
    private lateinit var backButton: TextView
    private lateinit var switchControlButton: TextView
    private lateinit var settingsLayout: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_settings)

        initViews()

        isButtonOn = intent.getBooleanExtra("isButtonOn", false)

        updateBackgroundColor()
        setupClickListeners()

        val intent = intent
        if (intent != null && intent.hasExtra("isButtonOn")) {
            isButtonOn = intent.getBooleanExtra("isButtonOn", false)
            updateMargin()
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.activity_settings)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val buttonClicked = intent.getStringExtra("button_clicked")
        setupHeader(buttonClicked)
    }

    // Функция для конвертации dp в px
    private fun Int.dpToPx(): Int {
        return (this * resources.displayMetrics.density).toInt()
    }

    // Функция для изменения layout_marginStart
    private fun updateMargin() {
        val shouldUseNegativeMargin = isButtonOn
        val params = backButton.layoutParams as ViewGroup.MarginLayoutParams
        params.marginStart = if (shouldUseNegativeMargin) -25.dpToPx() else 0
        backButton.layoutParams = params
    }

    private fun hideOtherViews() {
        findViewById<TextView>(R.id.switch_control).visibility = View.GONE
        findViewById<TextView>(R.id.share).visibility = View.GONE
        findViewById<TextView>(R.id.group).visibility = View.GONE
        findViewById<TextView>(R.id.agreement).visibility = View.GONE
    }

    private fun setupHeader(buttonClicked: String?) {
        val titleTextView = findViewById<TextView>(R.id.title)

        when (buttonClicked) {
            "search" -> {
                hideOtherViews()
                titleTextView.text = getString(R.string.search)
            }
            "media" -> {
                hideOtherViews()
                titleTextView.text = getString(R.string.media)
            }
            else -> titleTextView.text = getString(R.string.list0)
        }
    }

    private fun initViews() {
        switchControlButton = findViewById(R.id.switch_control)
        backButton = findViewById(R.id.title)
        settingsLayout = findViewById(R.id.activity_settings)
    }

    private fun setupClickListeners() {
        switchControlButton.setOnClickListener {
            isButtonOn = !isButtonOn
            updateMargin()
            updateBackgroundColor() // Обновляем UI при переключении
        }

        backButton.setOnClickListener {
            val intent = Intent().apply {
                putExtra("isButtonOn", isButtonOn)
            }
            setResult(RESULT_OK, intent)
            finish()
        }
    }

    private fun updateBackgroundColor() {
        val backgroundColor = if (isButtonOn) {
            ContextCompat.getColor(this, R.color.black)
        } else {
            ContextCompat.getColor(this, R.color.white)
        }
        settingsLayout.setBackgroundColor(backgroundColor)

        val switchDrawableRes = if (isButtonOn) {
            R.drawable.control1
        } else {
            R.drawable.control
        }
        // Установка правого Drawable по ресурсу
        switchControlButton.setCompoundDrawablesWithIntrinsicBounds(0, 0, switchDrawableRes, 0)

        // Получаем Drawable из ресурса
        val drawable = ContextCompat.getDrawable(this, R.drawable.arrow_back)

        // Проверяем состояние и задаем цвет
        val color = if (isButtonOn) {
            ContextCompat.getColor(this, R.color.black)
        } else {
            ContextCompat.getColor(this, R.color.transparent)
        }

        // Устанавливаем цвет стрелки
        drawable?.let {
            DrawableCompat.setTint(it, color)  // Меняем цвет
            backButton.setCompoundDrawablesWithIntrinsicBounds(it, null, null, null) // Устанавливаем стрелку
        }

        // Обновляем цвет текста для всех TextView в LinearLayout
        val textColor = if (isButtonOn) {
            ContextCompat.getColor(this, R.color.white) // Цвет текста, когда кнопка включена
        } else {
            ContextCompat.getColor(this, R.color.textColor) // Цвет текста, когда кнопка выключена
        }

        // Изменяем цвет текста всех TextView внутри LinearLayout
        for (i in 0 until settingsLayout.childCount) {
            val view = settingsLayout.getChildAt(i)
            if (view is TextView) {
                view.setTextColor(textColor)
            }
        }

        val titleTextView = findViewById<TextView>(R.id.title)

        // Пример изменения цвета текста в зависимости от условия
        val colorTitle = if (isButtonOn) {
            ContextCompat.getColor(this, R.color.white) // Здесь используйте свой цвет
        } else {
            ContextCompat.getColor(this, R.color.textColor)
        }

        // Установка цвета текста
        titleTextView.setTextColor(colorTitle)

    }
}