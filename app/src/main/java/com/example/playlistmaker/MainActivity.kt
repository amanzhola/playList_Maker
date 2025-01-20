package com.example.playlistmaker

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var settingsActivityHelper: SettingsActivityHelper

    @SuppressLint("ResourceAsColor")
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        // 1ый экран тело
        setContentView(R.layout.activity_main)

//        Убрать код Toast из обработчиков нажатия на кнопки.
//        убрано через комментирование
//        setupFirstScreen()

        // ночной режим 1ый экран
//        findViewById<View>(R.id.activity_main).setBackgroundColor(ContextCompat.getColor(this, R.color.black))


        // 2ой экран тело
//        setContentView(R.layout.activity_settings)

//        2ой экран дневной и ночной режим
//        settingsActivityHelper = SettingsActivityHelper(this)
//        settingsActivityHelper.setupViews()


//         1ый экран
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.activity_main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        // 2ой экран
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.activity_settings)) { v, insets ->
//            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
//            insets
//        }

    }

    private fun setupFirstScreen() {

//        1. Implementation of an anonymous class

        val search = findViewById<View>(R.id.Button_Big1)
        val imageClickListener: View.OnClickListener = object : View.OnClickListener {
            override fun onClick(v: View?) {
                Toast.makeText(this@MainActivity, "Нажали на поиск!", Toast.LENGTH_SHORT).show()
            }
        }
        search.setOnClickListener(imageClickListener)

//         2. Lambda expression
        val media = findViewById<View>(R.id.Button_Big2)

        media.setOnClickListener {
            Toast.makeText(this@MainActivity, "Нажали на медиа!", Toast.LENGTH_SHORT).show()
        }

//        3. Implementing OnClickListener on an Activity
        val settings = findViewById<View>(R.id.Button_Big3)
        settings.setOnClickListener(this@MainActivity)

    }

    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.Button_Big3 -> {
                Toast.makeText(this, "Нажали на настройки!", Toast.LENGTH_SHORT).show()
            }
        }
    }


}