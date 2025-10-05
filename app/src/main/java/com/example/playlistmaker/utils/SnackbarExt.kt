package com.example.playlistmaker.utils

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar

// for WeatherSearch Activity to replace Toasts
fun Activity.showLongSnack(text: CharSequence, durationMs: Int = 3000, anchor: View? = null) {
    val root = findViewById<View>(android.R.id.content)
    val sb = Snackbar.make(root, text, Snackbar.LENGTH_LONG)
    (sb.view.layoutParams as? ViewGroup.MarginLayoutParams)?.setMargins(0, 0, 0, 0)
    anchor?.let { sb.anchorView = it }
    sb.duration = durationMs
    sb.show()
}

fun Fragment.showLongSnack(text: CharSequence, durationMs: Int = 3000, anchor: View? = null) {
    val root: View = requireActivity().findViewById(android.R.id.content)
    val sb = Snackbar.make(root, text, Snackbar.LENGTH_LONG)
    (sb.view.layoutParams as? ViewGroup.MarginLayoutParams)?.setMargins(0, 0, 0, 0)
    anchor?.let { sb.anchorView = it }
    sb.duration = durationMs
    sb.show()
}
