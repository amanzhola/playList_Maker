package com.example.playlistmaker.utils

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.playlistmaker.BaseActivity
import com.example.playlistmaker.R
import com.example.playlistmaker.presentation.utils.activityHelper.FailUiController

/**
 * Пытаемся собрать FailUiController:
 * 1) сначала корень текущего фрагмента и его failText,
 * 2) затем корень активити и её failText,
 * 3) если чего-то не хватает — вернёт null (используйте Snackbar).
 */
fun Activity.resolveFailUiControllerOrNull(): FailUiController? {
    val fragmentRoot: ViewGroup? =
        (this as? BaseActivity)?.getCurrentFragment()?.view as? ViewGroup

    val activityRoot: ViewGroup? = when (this) {
        is BaseActivity -> findViewById((this as BaseActivity).getMainLayoutId())
        else -> findViewById(android.R.id.content) as? ViewGroup
    }

    val root = fragmentRoot ?: activityRoot ?: return null

    val errorTextFromFragment: TextView? =
        (fragmentRoot?.findViewById<View>(R.id.failText) as? TextView)
            ?: fragmentRoot?.findViewById(R.id.fail)

    val errorTextFromActivity: TextView? =
        (findViewById<View>(R.id.failText) as? TextView)
            ?: findViewById(R.id.fail)

    val errorText = errorTextFromFragment ?: errorTextFromActivity ?: return null

    return FailUiController(this, root, errorText)
}

/** Показать ошибку: сначала через FailUiController, если нет — Snackbar (якорим к bottomNavigation, если есть). */
fun Activity.showFailOrSnack(isSupport: Boolean, durationMs: Int = 5000) {
    val failUi = resolveFailUiControllerOrNull()
    if (failUi != null) {
        failUi.showTemporaryError(isSupport)
    } else {
        // Snackbar-резерв: многострочный и с якорем
        showLongSnack(
            text = getString(if (isSupport) R.string.supportEmail else R.string.networkFail),
            durationMs = durationMs,
            anchorId = R.id.bottomNavigation
        )
    }
}

/** Для фрагментов — просто форвардим на Activity (учитываем, что activity может быть null). */
fun Fragment.showFailOrSnack(isSupport: Boolean, durationMs: Int = 4000) {
    activity?.showFailOrSnack(isSupport, durationMs)
}
