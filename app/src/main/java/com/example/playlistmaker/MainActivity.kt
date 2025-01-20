package com.example.playlistmaker

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity(), View.OnClickListener {

    @SuppressLint("ResourceAsColor")
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

//        1ый экран тело
        setContentView(R.layout.activity_main)
        setupFirstScreen()

//        1ый экран 3 кнопки без шапки
//        findViewById<LinearLayout>(R.id.PanelHeader).visibility = View.GONE

//        ночной режим 1ый экран
//        findViewById<View>(R.id.activity_main).setBackgroundColor(ContextCompat.getColor(this, R.color.black))

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.activity_main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

    }

    private fun setupFirstScreen() {

//        1. Implementation of an anonymous class
        val search = findViewById<View>(R.id.Button_Big1)
        val imageClickListener: View.OnClickListener = object : View.OnClickListener {
            override fun onClick(v: View?) {

                val intent = Intent(this@MainActivity, SettingsActivity::class.java)
                intent.putExtra("button_clicked", "search")
                startActivity(intent)

            }
        }
        search.setOnClickListener(imageClickListener)

//         2. Lambda expression
        val media = findViewById<View>(R.id.Button_Big2)

        media.setOnClickListener {
            val intent = Intent(this@MainActivity, SettingsActivity::class.java)
            intent.putExtra("button_clicked", "media")
            startActivity(intent)
        }

//        3. Implementing OnClickListener on an Activity
        val settings = findViewById<View>(R.id.Button_Big3)
        settings.setOnClickListener(this@MainActivity)

    }

    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.Button_Big3 -> {
                val displayIntent = Intent(this@MainActivity, SettingsActivity::class.java)
                startActivity(displayIntent)
            }
        }
    }


}