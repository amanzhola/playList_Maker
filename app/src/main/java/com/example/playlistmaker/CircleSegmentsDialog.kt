package com.example.playlistmaker

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Window

// 💐 (U+1F490) 🌸(U+1F338) 🌺 (U+1F33A) 🌼(U+1F33) 🌻(U+1F33B) 🌷(U+1F337)
class CircleSegmentsDialog(context: Context, private val listener: CircleSegmentsView.OnSegmentClickListener) : Dialog(context) {
    // ☝️ 💯 интерфейс OnSegmentClickListener <-> BaseActivity(done)

    private lateinit var circleSegmentsView: CircleSegmentsView
    private var pendingData: (() -> Unit)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.dialog_circle_segments)

        window?.setBackgroundDrawableResource(android.R.color.transparent)

        circleSegmentsView = findViewById(R.id.circleSegmentsView)

        circleSegmentsView.setOnSegmentClickListener(object :
            CircleSegmentsView.OnSegmentClickListener {
            override fun onSegmentClicked(segmentIndex: Int, isChangedState: Boolean) {
                listener.onSegmentClicked(segmentIndex, isChangedState)
                dismiss()
            }
        })

        pendingData?.invoke()
        pendingData = null
    }

    fun setSegmentData(
        colors: IntArray, texts: Array<String>, icons: IntArray,
        newColors: IntArray, newTexts: Array<String>, newIcons: IntArray,
        total: Int, newTotal: Int
    ) {
        val applyData = {
            circleSegmentsView.setSegmentData(colors, texts, icons, newColors, newTexts, newIcons, total, newTotal)
        }

        if (::circleSegmentsView.isInitialized) {
            applyData()
        } else {
            pendingData = applyData
        }
    }
}