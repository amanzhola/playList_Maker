package com.example.playlistmaker.presentation.utils

import android.app.Activity
import com.example.playlistmaker.CircleSegmentsDialog
import com.example.playlistmaker.CircleSegmentsView

// 💐 (U+1F490) 🌸(U+1F338) 🌺 (U+1F33A) 🌼(U+1F33) 🌻(U+1F33B) 🌷(U+1F337)
class SegmentHelper(
    private val activity: Activity,
    private val listener: CircleSegmentsView.OnSegmentClickListener
) {

    private var dialog: CircleSegmentsDialog? = null

    fun showSegmentDialog(
        segmentColors: IntArray,
        segmentTexts: Array<String>,
        segmentIcons: IntArray,
        newSegmentColors: IntArray,
        newSegmentTexts: Array<String>,
        newSegmentIcons: IntArray,
        totalSegments: Int,
        newTotalSegments: Int,
        onDismiss: (() -> Unit)? = null
    ) {
        dialog = CircleSegmentsDialog(activity, listener).apply {
            setOnDismissListener {
                onDismiss?.invoke()
            }

            setSegmentData(
                segmentColors,
                segmentTexts,
                segmentIcons,
                newSegmentColors,
                newSegmentTexts,
                newSegmentIcons,
                totalSegments,
                newTotalSegments
            )

            show()
        }
    }

    fun isDialogVisible(): Boolean {
        return dialog?.isShowing == true
    }
}
