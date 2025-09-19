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

/** N строк, конец обрезаем троеточием.(не используется заменен с скрол когда 1 строка) */
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

fun setupDesc(tv: TextView, maxLines: Int) {
    if (maxLines > 1) {
        // Много строк, без горизонтального скролла
        tv.isSingleLine = false
        tv.setHorizontallyScrolling(false)
        tv.maxLines = maxLines
        tv.ellipsize = TextUtils.TruncateAt.END

        // важное: выключаем marquee
        tv.marqueeRepeatLimit = 0
        tv.isSelected = false
    } else {
        // ОДНА строка, горизонтальный скролл через marquee
        tv.isSingleLine = true
        tv.setHorizontallyScrolling(true)
        tv.maxLines = 1
        tv.ellipsize = TextUtils.TruncateAt.MARQUEE
        tv.marqueeRepeatLimit = -1      // бесконечно
        tv.isSelected = true            // триггерит marquee без фокуса
        tv.isFocusable = false
        tv.isFocusableInTouchMode = false
    }
}

