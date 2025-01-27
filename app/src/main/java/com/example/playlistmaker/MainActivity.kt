package com.example.playlistmaker

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity(), View.OnClickListener {

    private var isButtonOn = false
    private lateinit var mainLayout: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mainLayout = findViewById(R.id.activity_main)
        setupFirstScreen()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.activity_main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun setupFirstScreen() {
        val search = findViewById<View>(R.id.Button_Big1)
        val imageClickListener: View.OnClickListener = object : View.OnClickListener {

            override fun onClick(v: View?) {
                val intent = Intent(this@MainActivity, SettingsActivity::class.java)
                intent.putExtra("button_clicked", "search")
                intent.putExtra("isButtonOn", isButtonOn)
                startActivityForResult(intent, REQUEST_CODE_SETTINGS)
            }
        }
        search.setOnClickListener(imageClickListener)

        val media = findViewById<View>(R.id.Button_Big2)
        media.setOnClickListener {
            val intent = Intent(this@MainActivity, SettingsActivity::class.java)
            intent.putExtra("button_clicked", "media")
            intent.putExtra("isButtonOn", isButtonOn)
            startActivityForResult(intent, REQUEST_CODE_SETTINGS)
        }

        val settings = findViewById<View>(R.id.Button_Big3)
        settings.setOnClickListener(this@MainActivity)
    }

    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.Button_Big3 -> {
                val intent = Intent(this@MainActivity, SettingsActivity::class.java)
                intent.putExtra("isButtonOn", isButtonOn)
                startActivityForResult(intent, REQUEST_CODE_SETTINGS)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_SETTINGS && resultCode == RESULT_OK) {
            isButtonOn = data?.getBooleanExtra("isButtonOn", false) ?: false
            updateBackgroundColor()
        }
    }

    private fun updateBackgroundColor() {
        val backgroundColor = if (isButtonOn) {
            ContextCompat.getColor(this, R.color.black)
        } else {
            ContextCompat.getColor(this, R.color.backgroundDay)
        }
        mainLayout.setBackgroundColor(backgroundColor)
    }

    companion object {
        private const val REQUEST_CODE_SETTINGS = 1
    }
}