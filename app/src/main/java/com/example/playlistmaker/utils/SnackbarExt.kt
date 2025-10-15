package com.example.playlistmaker.utils

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar

// Activity-версия — можно оставить как есть (root = content view)
fun Activity.showLongSnack(
    text: CharSequence,
    durationMs: Int = 3000,
    anchor: View? = null
) {
    val root = findViewById<View>(android.R.id.content) ?: return
    val sb = Snackbar.make(root, text, Snackbar.LENGTH_LONG)
    (sb.view.layoutParams as? ViewGroup.MarginLayoutParams)?.setMargins(0, 0, 0, 0)
    anchor?.let { sb.setAnchorView(it) }   // предпочтительнее, чем прямой доступ к anchorView
    sb.duration = durationMs
    sb.show()
}

// Fragment-версия — БЕЗ requireActivity(), привязываемся к view фрагмента.
// Если view уже уничтожена, просто выходим (ничего не показываем).
fun Fragment.showLongSnack(
    text: CharSequence,
    durationMs: Int = 3000,
    anchor: View? = null
) {
    // сначала пробуем корневую view фрагмента; если её уже нет — фоллбэк к content view активити
    val root: View = view ?: activity?.findViewById(android.R.id.content) ?: return
    val sb = Snackbar.make(root, text, Snackbar.LENGTH_LONG)
    (sb.view.layoutParams as? ViewGroup.MarginLayoutParams)?.setMargins(0, 0, 0, 0)
    anchor?.let { sb.setAnchorView(it) }
    sb.duration = durationMs
    sb.show()
}
