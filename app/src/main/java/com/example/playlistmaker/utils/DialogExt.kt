package com.example.playlistmaker.utils

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.example.playlistmaker.R

@SuppressLint("UseKtx")
fun AlertDialog.showWithSquareWhiteStyle(ctx: Context) {
    setOnShowListener {
        // 1) окно: без затемнения и без собственного фона
        window?.setDimAmount(0f)
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        // 2) Фон самой карточки (контейнера) — ставим твой drawable
        val cardBg = ContextCompat.getDrawable(ctx, R.drawable.bg_dialog_square)

        // Основная родительская панель AlertDialog'а
        findViewById<ViewGroup>(androidx.appcompat.R.id.parentPanel)?.background = cardBg

        // На всякий случай обнулим фон внутренних панелей, чтобы не подсвечивали края
        listOf(
            androidx.appcompat.R.id.topPanel,
            androidx.appcompat.R.id.contentPanel,
            androidx.appcompat.R.id.buttonPanel
        ).forEach { id ->
            findViewById<View>(id)?.setBackgroundColor(Color.TRANSPARENT)
        }

        // Кнопки/тексты — как было
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

fun AlertDialog.findTitleView(): TextView? {
    return findViewById(androidx.appcompat.R.id.alertTitle)
        ?: findViewById(com.google.android.material.R.id.alertTitle)
        ?: findViewById(android.R.id.title)
}
