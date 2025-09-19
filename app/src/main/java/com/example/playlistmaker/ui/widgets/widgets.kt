package com.example.playlistmaker.ui.widgets

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.annotation.DrawableRes
import androidx.appcompat.content.res.AppCompatResources
import com.example.playlistmaker.R

class PlaybackButtonView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var isPlaying: Boolean = false

    private var bmpPlay: Bitmap? = null
    private var bmpPause: Bitmap? = null
    private val dstRect = RectF()
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    /** Колбэк: сообщаем наружу, что пользователь хочет переключить воспроизведение */
    var onToggleRequested: ((isPlayingNow: Boolean) -> Unit)? = null

    init {
        isClickable = true
        isFocusable = true

        if (attrs != null) {
            val a = context.obtainStyledAttributes(attrs, R.styleable.PlaybackButtonView, defStyleAttr, 0)
            val playRes  = a.getResourceId(R.styleable.PlaybackButtonView_iconPlay, 0)
            val pauseRes = a.getResourceId(R.styleable.PlaybackButtonView_iconPause, 0)
            a.recycle()
            if (playRes != 0 && pauseRes != 0) {
                setIcons(playRes, pauseRes)
            }
        }
        updateContentDescription()
    }

    /** Установить иконки из ресурсов */
    fun setIcons(@DrawableRes playRes: Int, @DrawableRes pauseRes: Int) {
        bmpPlay  = d2b(AppCompatResources.getDrawable(context, playRes))
        bmpPause = d2b(AppCompatResources.getDrawable(context, pauseRes))
        invalidate()
    }

    /** Установить состояние извне (например, когда трек завершился) */
    fun setPlaying(playing: Boolean) {
        if (isPlaying != playing) {
            isPlaying = playing
            updateContentDescription()
            invalidate()
        }
    }

    /** Локальное переключение + сигнал наружу (VM/Fragment) */
    fun toggle() {
        setPlaying(!isPlaying)                 // сразу меняем картинку
        onToggleRequested?.invoke(isPlaying)   // просим экран запустить/поставить на паузу
        performClick()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        val cw = (w - paddingLeft - paddingRight).toFloat()
        val ch = (h - paddingTop - paddingBottom).toFloat()
        val size = cw.coerceAtMost(ch)
        val left = paddingLeft + (cw - size) / 2f
        val top  = paddingTop + (ch - size) / 2f
        dstRect.set(left, top, left + size, top + size)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val b = if (isPlaying) bmpPause else bmpPlay
        b?.let { canvas.drawBitmap(it, null, dstRect, paint) }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> return true
            MotionEvent.ACTION_UP -> {
                if (event.x in 0f..width.toFloat() && event.y in 0f..height.toFloat()) {
                    toggle()
                    return true
                }
            }
        }
        return super.onTouchEvent(event)
    }
    private fun updateContentDescription() {
        contentDescription = if (isPlaying)
            context.getString(R.string.cd_pause) else context.getString(R.string.cd_play)
    }


    @SuppressLint("UseKtx")
    private fun d2b(drawable: Drawable?): Bitmap? {
        if (drawable == null) return null
        val w = drawable.intrinsicWidth.coerceAtLeast(48)
        val h = drawable.intrinsicHeight.coerceAtLeast(48)
        val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val c = Canvas(bmp)
        drawable.setBounds(0, 0, w, h)
        drawable.draw(c)
        return bmp
    }
}
