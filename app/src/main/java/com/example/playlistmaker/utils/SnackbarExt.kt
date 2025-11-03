package com.example.playlistmaker.utils

import android.app.Activity
import android.os.Looper
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.core.os.HandlerCompat
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar

// ---------- Вспомогательное: выполнение на главном потоке ----------
private fun runOnMain(block: () -> Unit) {
    if (Looper.myLooper() == Looper.getMainLooper()) {
        block()
    } else {
        HandlerCompat.createAsync(Looper.getMainLooper()).post(block)
    }
}

// ---------- Activity extensions ----------

fun Activity.showLongSnack(
    text: CharSequence,
    durationMs: Int = 3000,
    anchor: View? = null,
    anchorId: Int? = null,
) {
    val root = findViewById<View>(android.R.id.content) ?: return
    runOnMain {
        val sb = Snackbar.make(root, text, Snackbar.LENGTH_LONG)

        (sb.view.findViewById(com.google.android.material.R.id.snackbar_text) as? TextView)?.apply {
            isSingleLine = false
            maxLines = Int.MAX_VALUE
            ellipsize = null            // убираем "..."
            textAlignment = View.TEXT_ALIGNMENT_TEXT_START
            // setLineSpacing(0f, 1.1f) // если чуть увеличить межстрочный
        }

        // безопасно правим отступы, если это возможно
        (sb.view.layoutParams as? ViewGroup.MarginLayoutParams)?.let { lp ->
            lp.setMargins(lp.leftMargin, lp.topMargin, lp.rightMargin, lp.bottomMargin)
            sb.view.layoutParams = lp
        }

        // якорь: приоритет у явного View, потом у id
        val resolvedAnchor = anchor ?: anchorId?.let { findViewById<View>(it) }
        resolvedAnchor?.let { sb.setAnchorView(it) }

        sb.duration = durationMs
        sb.show()
    }
}

fun Activity.showLongSnack(
    @StringRes textRes: Int,
    durationMs: Int = 3000,
    anchor: View? = null,
    anchorId: Int? = null,
) = showLongSnack(getString(textRes), durationMs, anchor, anchorId)


// ---------- Fragment extensions ----------

fun Fragment.showLongSnack(
    text: CharSequence,
    durationMs: Int = 3000,
    anchor: View? = null,
    anchorId: Int? = null,
) {
    // 1) пробуем корневую view фрагмента (если живёт), 2) иначе — content view Activity
    val root: View = view ?: activity?.findViewById(android.R.id.content) ?: return
    runOnMain {
        val sb = Snackbar.make(root, text, Snackbar.LENGTH_LONG)

        (sb.view.findViewById(com.google.android.material.R.id.snackbar_text) as? TextView)?.apply {
            isSingleLine = false
            maxLines = Int.MAX_VALUE
            ellipsize = null            // убираем "..."
            textAlignment = View.TEXT_ALIGNMENT_TEXT_START
            // setLineSpacing(0f, 1.1f) // если чуть увеличить межстрочный
        }

        (sb.view.layoutParams as? ViewGroup.MarginLayoutParams)?.let { lp ->
            lp.setMargins(lp.leftMargin, lp.topMargin, lp.rightMargin, lp.bottomMargin)
            sb.view.layoutParams = lp
        }

        // anchor: используем явный, затем ищем по id сначала во фрагменте, потом в активити
        val resolvedAnchor = anchor
            ?: anchorId?.let { root.findViewById<View>(it) }
            ?: anchorId?.let { activity?.findViewById<View>(it) }

        resolvedAnchor?.let { sb.setAnchorView(it) }

        sb.duration = durationMs
        sb.show()
    }
}

fun Fragment.showLongSnack(
    @StringRes textRes: Int,
    durationMs: Int = 3000,
    anchor: View? = null,
    anchorId: Int? = null,
) = showLongSnack(getString(textRes), durationMs, anchor, anchorId)
