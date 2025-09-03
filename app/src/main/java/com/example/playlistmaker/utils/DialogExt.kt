package com.example.playlistmaker.utils

import android.content.Context
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.example.playlistmaker.R

/** Унифицированная стилизация диалога (фон, кнопки, текст). */
fun AlertDialog.showWithSquareWhiteStyle(ctx: Context) {
    setOnShowListener {
        window?.setBackgroundDrawable(
            ContextCompat.getDrawable(ctx, R.drawable.bg_dialog_square)
        )
        val accent = ContextCompat.getColor(ctx, R.color.backgroundDay)
        getButton(AlertDialog.BUTTON_POSITIVE)?.setTextColor(accent)
        getButton(AlertDialog.BUTTON_NEGATIVE)?.setTextColor(accent)

        findViewById<TextView>(android.R.id.message)
            ?.setTextColor(ContextCompat.getColor(ctx, R.color.textColor))

        findTitleView()
            ?.setTextColor(ContextCompat.getColor(ctx, R.color.textColor))
    }
    show()
}

/** Ищем TextView заголовка без getIdentifier (устар.). */
fun AlertDialog.findTitleView(): TextView? {
    return findViewById(androidx.appcompat.R.id.alertTitle)
        ?: findViewById(com.google.android.material.R.id.alertTitle)
        ?: findViewById(android.R.id.title)
}
