package com.example.playlistmaker

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Window

class CircleSegmentsDialog(context: Context, private val listener: BaseActivity) : Dialog(context) {

    private lateinit var circleSegmentsView: CircleSegmentsView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.dialog_circle_segments)

        // Устанавливаем прозрачный фон
        window?.setBackgroundDrawableResource(android.R.color.transparent)

        circleSegmentsView = findViewById(R.id.circleSegmentsView)

        circleSegmentsView.setOnSegmentClickListener(object : CircleSegmentsView.OnSegmentClickListener {
            override fun onSegmentClicked(segmentIndex: Int, isChangedState: Boolean) {
                listener.onSegmentClicked(segmentIndex, isChangedState)
                dismiss()
            }
        })
    }

    fun setSegmentData(colors: IntArray, texts: Array<String>, icons: IntArray, newColors: IntArray, newTexts: Array<String>, newIcons: IntArray, total: Int, newTotal: Int) {
        if (::circleSegmentsView.isInitialized) {
            circleSegmentsView.setSegmentData(colors, texts, icons, newColors, newTexts, newIcons, total, newTotal)
        }
    }

}