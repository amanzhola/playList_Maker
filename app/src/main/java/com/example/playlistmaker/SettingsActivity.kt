package com.example.playlistmaker

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.CompoundButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Switch
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class SettingsActivity : AppCompatActivity() {

    private lateinit var buttonSearch: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContentView(R.layout.activity_settings)

        // Получаем флаг из Intent, чтобы определить, какая кнопка была нажата
        val buttonClicked = intent.getStringExtra("button_clicked")

        // Устанавливаем текст заголовка и видимость LinearLayout в зависимости от нажатой кнопки
        when (buttonClicked) {
            "search" -> {
                findViewById<LinearLayout>(R.id.settings_light_mode).visibility = View.GONE
                findViewById<TextView>(R.id.x_Panel_Header_Middle_1Title).text = "Поиск"
            }
            "media" -> {
                findViewById<LinearLayout>(R.id.settings_light_mode).visibility = View.GONE
                findViewById<TextView>(R.id.x_Panel_Header_Middle_1Title).text = "Медиа"
            }
            else -> {
                findViewById<TextView>(R.id.x_Panel_Header_Middle_1Title).text = "Настройки"
            }
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.activity_settings)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val switchControl = findViewById<Switch>(R.id.switch_control)
        buttonSearch = findViewById(R.id.Button_Search_Light_ModeLeft)

        switchControl.setOnCheckedChangeListener { buttonView: CompoundButton, isChecked: Boolean ->

            if (isChecked) {
                findViewById<View>(R.id.activity_settings).setBackgroundColor(ContextCompat.getColor(this, R.color.black))
                findViewById<TextView>(R.id.switch_note).setTextColor(ContextCompat.getColor(this, R.color.white))
                findViewById<TextView>(R.id.x_Panel_Header_Middle_1Title).setTextColor(ContextCompat.getColor(this, R.color.white))
                findViewById<TextView>(R.id.switch_note).setTextColor(ContextCompat.getColor(this, R.color.white))
                findViewById<TextView>(R.id.share_note).setTextColor(ContextCompat.getColor(this, R.color.white))
                findViewById<TextView>(R.id.support_note).setTextColor(ContextCompat.getColor(this, R.color.white))
                findViewById<TextView>(R.id.agreement_note).setTextColor(ContextCompat.getColor(this, R.color.white))

                with(findViewById<TextView>(R.id.x_Panel_Header_Middle_1Title)) {
                    gravity = android.view.Gravity.START
                }

                buttonSearch.imageTintList = ContextCompat.getColorStateList(this, R.color.backgroundtransparent)

            } else {
                findViewById<View>(R.id.activity_settings).setBackgroundColor(ContextCompat.getColor(this, R.color.white))
                findViewById<TextView>(R.id.switch_note).setTextColor(ContextCompat.getColor(this, R.color.black))
                findViewById<TextView>(R.id.x_Panel_Header_Middle_1Title).setTextColor(ContextCompat.getColor(this, R.color.black))
                findViewById<TextView>(R.id.switch_note).setTextColor(ContextCompat.getColor(this, R.color.black))
                findViewById<TextView>(R.id.share_note).setTextColor(ContextCompat.getColor(this, R.color.black))
                findViewById<TextView>(R.id.support_note).setTextColor(ContextCompat.getColor(this, R.color.black))
                findViewById<TextView>(R.id.agreement_note).setTextColor(ContextCompat.getColor(this, R.color.black))

                with(findViewById<TextView>(R.id.x_Panel_Header_Middle_1Title)) {
                    gravity = android.view.Gravity.CENTER and android.view.Gravity.START
                }

                buttonSearch.imageTintList = ContextCompat.getColorStateList(this, R.color.black)
            }
        }

    }
}

