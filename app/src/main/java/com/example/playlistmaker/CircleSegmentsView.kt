package com.example.playlistmaker

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt

class CircleSegmentsView(context: Context?, attrs: AttributeSet?) : View(context, attrs) {

    private var isChangedState = false
    private var segmentClickListener: OnSegmentClickListener? = null

    private var paint: Paint? = null
    private var textPaint: Paint? = null
    private val segmentAngles = mutableListOf<Float>()

    private var segmentColors: IntArray = intArrayOf()
    private var newSegmentColors: IntArray = intArrayOf()
    private var segmentTexts: Array<String> = arrayOf()
    private var newSegmentTexts: Array<String> = arrayOf()
    private var segmentIcons: IntArray = intArrayOf()
    private var newSegmentIcons: IntArray = intArrayOf()
    private val smallCircleRadius = 45f

    private var totalSegments: Int = 0
    private var newTotalSegments: Int = 0

    init {
        init()
    }

    private fun init() {
        paint = Paint()
        textPaint = Paint()
        textPaint!!.color = Color.WHITE
        textPaint!!.textSize = 15f
        textPaint!!.textAlign = Paint.Align.CENTER
    }

    fun setSegmentData(colors: IntArray, texts: Array<String>, icons: IntArray, newColors: IntArray,
                       newTexts: Array<String>, newIcons: IntArray, total: Int, newTotal: Int) {

        this.segmentColors = colors
        this.segmentTexts = texts
        this.segmentIcons = icons
        this.newSegmentColors = newColors
        this.newSegmentTexts = newTexts
        this.newSegmentIcons = newIcons
        this.totalSegments = total
        this.newTotalSegments = newTotal
        invalidate()
    }

    @SuppressLint("DrawAllocation", "ResourceAsColor")
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val width = width
        val height = height
        val radius = (min(width.toDouble(), height.toDouble()) / 2).toInt()
        val centerX = width / 2
        val centerY = height / 2

        var startAngle = 0f
        val sweepAngle = 360f / (if (isChangedState) newTotalSegments else totalSegments) // 360 / N

        segmentAngles.clear()
        for (i in 0 until (if (isChangedState) newTotalSegments else totalSegments)) {
            paint!!.color = if (isChangedState)newSegmentColors[i] else segmentColors[i]
            canvas.drawArc(
                (centerX - radius).toFloat(), (centerY - radius).toFloat(),
                (centerX + radius).toFloat(), (centerY + radius).toFloat(),
                startAngle, sweepAngle, true, paint!!
            )

            segmentAngles.add(startAngle + sweepAngle / 2)

            val textX = (centerX + radius / 2 * cos(Math.toRadians((startAngle + sweepAngle / 2).toDouble()))).toFloat()
            val textY = (centerY + radius / 2 * sin(Math.toRadians((startAngle + sweepAngle / 2).toDouble()))).toFloat()
            (if (isChangedState) newSegmentTexts[i] else segmentTexts[i]).let {
                canvas.drawText(
                    it, textX, textY, textPaint!!)
            }

            val icon = if (isChangedState) newSegmentIcons[i] else segmentIcons[i]
            if (icon != 0) {
                val drawable: Drawable? = ContextCompat.getDrawable(context!!, icon)
                drawable?.let {
                    val bitmap = it.toBitmap()

                    // Изменяем цвет иконки на синий (Color.BLUE) или серый
                    val coloredBitmap = changeBitmapColor(bitmap, R.color.hintFieldColor )

                    val iconX = (centerX + radius * 0.75 * cos(Math.toRadians((startAngle + sweepAngle / 2).toDouble()))).toFloat()
                    val iconY = (centerY + radius * 0.75 * sin(Math.toRadians((startAngle + sweepAngle / 2).toDouble()))).toFloat()
                    val iconSize = 50 // размер иконки

                    val scaledIcon = Bitmap.createScaledBitmap(coloredBitmap, iconSize, iconSize, true)
                    canvas.drawBitmap(scaledIcon, iconX - iconSize / 2, iconY - iconSize / 2, null)
                }
            }

            startAngle += sweepAngle
        }

        paint!!.color = Color.RED // Устанавливаем цвет Color.RED
        canvas.drawCircle(centerX.toFloat(), centerY.toFloat(), smallCircleRadius, paint!!)

        val smallCircleText = if (isChangedState) context.getString(R.string.change) else context.getString(R.string.settings)
        textPaint!!.color = Color.WHITE
        textPaint!!.textSize = 17f // Размер текста
        textPaint!!.textAlign = Paint.Align.CENTER
        canvas.drawText(smallCircleText, centerX.toFloat(), centerY.toFloat() + textPaint!!.textSize / 4, textPaint!!)
    }

    private fun changeBitmapColor(sourceBitmap: Bitmap, color: Int): Bitmap {
        val resultBitmap = sourceBitmap.copy(Bitmap.Config.ARGB_8888, true)
        val paint = Paint()
        paint.colorFilter = PorterDuffColorFilter(color, PorterDuff.Mode.SRC_IN)
        val canvas = Canvas(resultBitmap)
        canvas.drawBitmap(resultBitmap, 0f, 0f, paint)
        return resultBitmap
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            val x = event.x
            val y = event.y
            handleTouch(x, y)
            return true
        }
        return super.onTouchEvent(event)
    }

    private fun handleTouch(x: Float, y: Float) {
        val centerX = width / 2
        val centerY = height / 2

        val dx = x - centerX
        val dy = y - centerY
        val distance = sqrt((dx * dx + dy * dy).toDouble()).toFloat()

        if (distance <= smallCircleRadius) {
            toggleSegmentState()
            return
        }

        val angle = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat()
        val normalizedAngle = (if (angle >= 0) angle else angle + 360) // нормализуем угол в [0, 360]

        for (i in 0 until (if (isChangedState) newTotalSegments else totalSegments)) {
            val startAngle = (360f / (if (isChangedState) newTotalSegments else totalSegments)) * i
            val endAngle = (360f / (if (isChangedState) newTotalSegments else totalSegments)) * (i + 1)

            if (normalizedAngle >= startAngle && normalizedAngle < endAngle) {
                segmentClickListener?.onSegmentClicked(i, isChangedState)
                return
            }
        }
    }

    private fun toggleSegmentState() {
        isChangedState = !isChangedState
        invalidate()
    }

    fun setOnSegmentClickListener(listener: OnSegmentClickListener) {
        this.segmentClickListener = listener
    }

    interface OnSegmentClickListener {
        fun onSegmentClicked(segmentIndex: Int, isChangedState: Boolean)
    }

}