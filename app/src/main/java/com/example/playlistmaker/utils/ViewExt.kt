package com.example.playlistmaker.utils

import android.text.TextUtils
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.core.view.updatePadding
import kotlin.math.roundToInt

/** Один ряд, конец обрезаем троеточием. */
fun TextView.makeSingleLineEllipsizeEnd() {
    isSingleLine = true
    maxLines = 1
    setHorizontallyScrolling(true)
    ellipsize = TextUtils.TruncateAt.END
}

/** N строк, конец обрезаем троеточием. */
fun TextView.makeEllipsizeEnd(max: Int) {
    isSingleLine = false
    setHorizontallyScrolling(false)
    maxLines = max
    ellipsize = TextUtils.TruncateAt.END
}

/** Иконка слева + отступ между иконкой и текстом. */
fun TextView.setStartDrawable(@DrawableRes drawableRes: Int, paddingPx: Int) {
    setCompoundDrawablesWithIntrinsicBounds(drawableRes, 0, 0, 0)
    compoundDrawablePadding = paddingPx
}

/** Паддинг сверху в dp. Остальные паддинги сохраняются. */
fun TextView.setTopPaddingDp(dp: Int) {
    val px = (dp * resources.displayMetrics.density).roundToInt()
    updatePadding(top = px)
}
