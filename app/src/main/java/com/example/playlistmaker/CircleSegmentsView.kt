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
import android.graphics.drawable.Drawable
import androidx.core.graphics.drawable.toBitmap
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

class CircleSegmentsView(context: Context, attrs: AttributeSet?) : View(context, attrs) {

    private var segmentClickListener: OnSegmentClickListener? = null
    private var paint: Paint? = null
    private var textPaint: Paint? = null
    private val segmentAngles = mutableListOf<Float>()
    private val segmentColors = intArrayOf(
        R.color.hintFieldColor,
        R.color.blue,
        R.color.hintFieldColor,
        R.color.blue,
        Color.DKGRAY // Color.TRANSPARENT
    )

    private val segmentTexts = arrayOf(
        context.getString(R.string.switch_short), // "Switch"
        context.getString(R.string.share_short), // "Share"
        context.getString(R.string.support_short), //  "Support"
        context.getString(R.string.agreement_short), // "Agreement"
        context.getString(R.string.navigation) // "Navigation"
    )

    private val segmentIcons = arrayOfNulls<Bitmap>(segmentTexts.size)
    private val totalSegments = 5

    init {
        init()
    }

    private fun init() {
        paint = Paint()
        textPaint = Paint()
        textPaint!!.color = Color.WHITE
        textPaint!!.textSize = 10f
        textPaint!!.textAlign = Paint.Align.CENTER
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        initIcons()
    }

    private fun initIcons() {
        val iconResourceIds = arrayOf(
            R.drawable.control_selector,
            R.drawable.share,
            R.drawable.group,
            R.drawable.vector,
            R.drawable.navigation_24
        )

        for (i in 0 until segmentIcons.size) {
            val drawable: Drawable? = ContextCompat.getDrawable(context, iconResourceIds[i])

            drawable?.let {
                val bitmap = it.toBitmap()
                segmentIcons[i] = changeBitmapColor(bitmap, Color.BLUE) // Color.LTGRAY
            }
        }
    }

    private fun changeBitmapColor(sourceBitmap: Bitmap, color: Int): Bitmap {
        val resultBitmap = sourceBitmap.copy(Bitmap.Config.ARGB_8888, true)
        val paint = Paint()
        paint.colorFilter = PorterDuffColorFilter(color, PorterDuff.Mode.SRC_IN)
        val canvas = Canvas(resultBitmap)
        canvas.drawBitmap(resultBitmap, 0f, 0f, paint)
        return resultBitmap
    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val width = width
        val height = height
        val radius = (min(width.toDouble(), height.toDouble()) / 2).toInt()
        val centerX = width / 2
        val centerY = height / 2

        var startAngle = 0f
        val sweepAngle = 360f / totalSegments

        segmentAngles.clear()
        for (i in 0 until totalSegments) {
            paint!!.color = segmentColors[i]
            canvas.drawArc(
                (centerX - radius).toFloat(), (centerY - radius).toFloat(),
                (centerX + radius).toFloat(), (centerY + radius).toFloat(),
                startAngle, sweepAngle, true, paint!!
            )

            segmentAngles.add(startAngle + sweepAngle / 2)

            val textRadius = radius / 2
            val textX = (centerX + textRadius * cos(Math.toRadians((startAngle + sweepAngle / 2).toDouble()))).toFloat()
            val textY = (centerY + textRadius * sin(Math.toRadians((startAngle + sweepAngle / 2).toDouble()))).toFloat()

            val iconRadius = radius * 0.75

            val iconX = (centerX + iconRadius * cos(Math.toRadians((startAngle + sweepAngle / 2).toDouble()))).toFloat()
            val iconY = (centerY + iconRadius * sin(Math.toRadians((startAngle + sweepAngle / 2).toDouble()))).toFloat()

            canvas.drawText(segmentTexts[i], textX, textY, textPaint!!)

            val icon = segmentIcons[i]
            icon?.let {
                val iconWidth = 50
                val iconHeight = 50
                val scaledIcon = Bitmap.createScaledBitmap(it, iconWidth, iconHeight, true)
                canvas.drawBitmap(scaledIcon, iconX - iconWidth / 2, iconY - iconHeight / 2, null)
            }

            startAngle += sweepAngle
        }
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
        val angle = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat()

        val normalizedAngle = (if (angle >= 0) angle else angle + 360)

        for (i in 0 until totalSegments) {
            val startAngle = (360f / totalSegments) * i
            val endAngle = (360f / totalSegments) * (i + 1)

            if (normalizedAngle >= startAngle && normalizedAngle < endAngle) {
                segmentClickListener?.onSegmentClicked(i)
                return
            }
        }
    }

    fun setOnSegmentClickListener(listener: OnSegmentClickListener) {
        this.segmentClickListener = listener
    }

    interface OnSegmentClickListener {
        fun onSegmentClicked(segmentIndex: Int)
    }
}