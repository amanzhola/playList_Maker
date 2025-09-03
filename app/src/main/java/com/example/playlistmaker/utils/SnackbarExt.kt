package com.example.playlistmaker.utils

import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar

fun Fragment.showLongSnack(text: CharSequence, durationMs: Int = 4000) {
    val root = requireActivity().findViewById(android.R.id.content) as android.view.View
    val sb = Snackbar.make(root, text, Snackbar.LENGTH_LONG)
    (sb.view.layoutParams as? android.view.ViewGroup.MarginLayoutParams)?.setMargins(0, 0, 0, 0)
    sb.duration = durationMs
    sb.show()
}
