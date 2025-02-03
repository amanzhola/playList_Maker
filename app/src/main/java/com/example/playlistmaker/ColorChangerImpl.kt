package com.example.playlistmaker

import android.content.Context
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat

class ColorChangerImpl(private val context: Context) : ColorChanger {
    override fun changeBackgroundColor(view: View, backgroundColors: Pair<Int, Int>) {
        view.setBackgroundColor(ContextCompat.getColor(context, backgroundColors.first))
    }

    override fun changeTextColor(textView: TextView, textColor: Int) {
        textView.setTextColor(ContextCompat.getColor(context, textColor))
    }
}