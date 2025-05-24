package com.example.playlistmaker

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.core.graphics.scale
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt

// 💐 (U+1F490) 🌸(U+1F338) 🌺 (U+1F33A) 🌼(U+1F33) 🌻(U+1F33B) 🌷(U+1F337)
class CircleSegmentsView(context: Context?, attrs: AttributeSet?) : View(context, attrs) {
// 🌀 👆 🔄 🚫
    private var isChangedState = false
    private var segmentClickListener: OnSegmentClickListener? = null

    private val paint = Paint()
    private val textPaint = Paint().apply {
        color = Color.WHITE
        textAlign = Paint.Align.CENTER
    }

    private var segmentColors = intArrayOf()
    private var newSegmentColors = intArrayOf()
    private var segmentTexts = arrayOf<String>()
    private var newSegmentTexts = arrayOf<String>()
    private var segmentIcons = intArrayOf()
    private var newSegmentIcons = intArrayOf()

    private var cachedIcons: List<Bitmap>? = null
    private var cachedNewIcons: List<Bitmap>? = null

    private var totalSegments: Int = 0
    private var newTotalSegments: Int = 0

    private val smallCircleRadius by lazy { resources.getDimension(R.dimen.small_circle_radius) }
    private val textSizeSmall by lazy { resources.getDimension(R.dimen.text_size_small) }
    private val textSizeLarge by lazy { resources.getDimension(R.dimen.text_size_large) }
    private val iconSize by lazy { resources.getDimension(R.dimen.icon_size).toInt() }

    private var rotationAngle = 0f
    private var lastTouchAngle: Float? = null
    private var wasRotated = false
    private var clickHandled = false

    fun setSegmentData(
        colors: IntArray, texts: Array<String>, icons: IntArray,
        newColors: IntArray, newTexts: Array<String>, newIcons: IntArray,
        total: Int, newTotal: Int
    ) {
        this.segmentColors = colors
        this.segmentTexts = texts
        this.segmentIcons = icons
        this.newSegmentColors = newColors
        this.newSegmentTexts = newTexts
        this.newSegmentIcons = newIcons
        this.totalSegments = total
        this.newTotalSegments = newTotal

        cachedIcons = cacheIcons(icons)
        cachedNewIcons = cacheIcons(newIcons)

        invalidate()
    }

    private fun cacheIcons(icons: IntArray): List<Bitmap> {
        return icons.toList().mapNotNull { id ->
            ContextCompat.getDrawable(context!!, id)?.toBitmap()?.let { bitmap ->
                val color = ContextCompat.getColor(context!!, R.color.hintFieldColor)
                changeBitmapColor(bitmap, color).scale(iconSize, iconSize)
            }
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val currentColors = if (isChangedState) newSegmentColors else segmentColors
        val currentTexts = if (isChangedState) newSegmentTexts else segmentTexts
        val currentIcons = if (isChangedState) cachedNewIcons else cachedIcons
        val currentTotal = if (isChangedState) newTotalSegments else totalSegments

        val width = width
        val height = height
        val radius = min(width, height) / 2
        val centerX = width / 2
        val centerY = height / 2

        val sweepAngle = 360f / currentTotal
        var startAngle = rotationAngle % 360

        textPaint.textSize = textSizeSmall
        textPaint.color = Color.WHITE

        for (i in 0 until currentTotal) {
            paint.color = currentColors[i]

            canvas.drawArc(
                (centerX - radius).toFloat(), (centerY - radius).toFloat(),
                (centerX + radius).toFloat(), (centerY + radius).toFloat(),
                startAngle, sweepAngle, true, paint
            )

            val midAngle = Math.toRadians((startAngle + sweepAngle / 2).toDouble())
            val textX = centerX + radius / 2 * cos(midAngle)
            val textY = centerY + radius / 2 * sin(midAngle)

            canvas.drawText(currentTexts[i], textX.toFloat(), textY.toFloat(), textPaint)

            currentIcons?.getOrNull(i)?.let { iconBitmap ->
                val iconX = centerX + radius * 0.75 * cos(midAngle)
                val iconY = centerY + radius * 0.75 * sin(midAngle)
                canvas.drawBitmap(iconBitmap, iconX.toFloat() - iconSize / 2, iconY.toFloat() - iconSize / 2, null)
            }

            startAngle += sweepAngle
        }

        // Центральный круг
        paint.color = Color.RED
        canvas.drawCircle(centerX.toFloat(), centerY.toFloat(), smallCircleRadius, paint)

        textPaint.textSize = textSizeLarge
        canvas.drawText(
            if (isChangedState) context.getString(R.string.change) else context.getString(R.string.settings),
            centerX.toFloat(), centerY.toFloat() + textPaint.textSize / 4, textPaint
        )
    }

    private fun changeBitmapColor(source: Bitmap, color: Int): Bitmap {
        val result = source.copy(Bitmap.Config.ARGB_8888, true)
        val paint = Paint().apply {
            colorFilter = PorterDuffColorFilter(color, PorterDuff.Mode.SRC_IN)
        }
        Canvas(result).drawBitmap(result, 0f, 0f, paint)
        return result
    }

    @SuppressLint("ClickableViewAccessibility") // 🌀 👇 😎 (вращение)
    override fun onTouchEvent(event: MotionEvent): Boolean {
        val centerX = width / 2f
        val centerY = height / 2f
        val dx = event.x - centerX
        val dy = event.y - centerY
        val distance = sqrt(dx * dx + dy * dy)
        val touchAngle = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat()
        val normalizedAngle = if (touchAngle >= 0) touchAngle else touchAngle + 360

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                performClick()
                lastTouchAngle = normalizedAngle
                wasRotated = false
                clickHandled = false

                if (distance <= smallCircleRadius) {
                    toggleSegmentState()
                    clickHandled = true
                    return true
                }

                return true
            }

            MotionEvent.ACTION_MOVE -> { //  💫
                lastTouchAngle?.let { lastAngle ->
                    val delta = normalizedAngle - lastAngle
                    if (kotlin.math.abs(delta) > 1f) wasRotated = true
                    rotationAngle += delta
                    invalidate()
                    lastTouchAngle = normalizedAngle
                }
                return true
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                if (!wasRotated && !clickHandled && distance > smallCircleRadius) {
                    handleTouch(event.x, event.y) // 🔥 обработка нажатия на сегмент
                    clickHandled = true
                }
                lastTouchAngle = null
                return true
            }
        }
        return false
    }


    override fun performClick(): Boolean {
        super.performClick()
        return true
    }

    private fun toggleSegmentState() {
        isChangedState = !isChangedState
        invalidate()
    }

    fun setOnSegmentClickListener(listener: OnSegmentClickListener) {
        this.segmentClickListener = listener
    }

    private fun handleTouch(x: Float, y: Float) {
        if(clickHandled) return // ⛔ Защита от повторного вызова
        clickHandled = true  // ✅ Отметить, клик уже обработан

        val centerX = width / 2f
        val centerY = height / 2f
        val dx = x - centerX
        val dy = y - centerY
        val distance = sqrt(dx * dx + dy * dy)

        if (distance <= smallCircleRadius) {
            toggleSegmentState()
            return
        }

        val angle = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat()
        val adjustedAngle = (angle - rotationAngle + 360) % 360

        val currentTotal = if (isChangedState) newTotalSegments else totalSegments
        val segmentIndex = (adjustedAngle / (360f / currentTotal)).toInt()

        if (segmentIndex in 0 until currentTotal) {
            segmentClickListener?.onSegmentClicked(segmentIndex, isChangedState)
        }
    }

    interface OnSegmentClickListener {
        fun onSegmentClicked(segmentIndex: Int, isChangedState: Boolean)
    }
}