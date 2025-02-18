package com.example.playlistmaker

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Window

class CircleSegmentsDialog(context: Context, private val listener: BaseActivity) : Dialog(context) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.dialog_circle_segments)

        // Устанавливаем прозрачный фон
        window?.setBackgroundDrawableResource(android.R.color.transparent)

        val circleSegmentsView = findViewById<CircleSegmentsView>(R.id.circleSegmentsView)

        circleSegmentsView.setOnSegmentClickListener(listener)
    }
}