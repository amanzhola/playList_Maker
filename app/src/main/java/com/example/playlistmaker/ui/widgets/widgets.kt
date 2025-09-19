package com.example.playlistmaker.ui.widgets

import android.annotation.SuppressLint
import android.content.Context
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

/**Опция 2: Избавляемся от d2b(), храним Drawable и в onDraw() ставим им bounds под dstRect.**/
/**Плюсы: нет аллокаций bitmap в рантайме, отлично работает с VectorDrawable (масштабируется без потерь).**/
class PlaybackButtonView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var isPlaying = false

    private var playDrawable: Drawable? = null
    private var pauseDrawable: Drawable? = null

    private val dstRect = RectF()
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    /** Колбэк: сообщаем наружу, что пользователь хочет переключить воспроизведение */
    var onToggleRequested: ((Boolean) -> Unit)? = null

    init {
        isClickable = true       // говорит системе, что это кликабельная вью
        isFocusable = true       // доступна с клавиатуры/TV/TalkBack

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
        playDrawable  = AppCompatResources.getDrawable(context, playRes)
        pauseDrawable = AppCompatResources.getDrawable(context, pauseRes)
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
        val d = if (isPlaying) pauseDrawable else playDrawable
        d?.let {
            // подгоняем под целевой прямоугольник
            it.setBounds(
                dstRect.left.toInt(), dstRect.top.toInt(),
                dstRect.right.toInt(), dstRect.bottom.toInt()
            )
            it.state = drawableState        // чтобы работали pressed/focused стейты, если они есть
            it.setVisible(isShown, false)
            it.draw(canvas)
        }
    }

    /** Обработчик касаний – «канонический» паттерн */
    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                // Захватываем жест и даём визуальный pressed-стейт
                isPressed = true
                // Важный момент: вернуть true, чтобы получать последующие события UP/CANCEL
                return true
            }
            MotionEvent.ACTION_UP -> {
                // Снимаем pressed, проверяем, что палец ушёл внутри вью
                isPressed = false
                val inside = event.x in 0f..width.toFloat() && event.y in 0f..height.toFloat()
                if (inside) {
                    // Сообщаем системе о клике (доступность, звук/хаптик и т.д.)
                    return performClick()
                }
            }
            MotionEvent.ACTION_CANCEL -> {
                isPressed = false
            }
        }
        return super.onTouchEvent(event)
    }

    /**
     * performClick должен вызывать бизнес-логику клика (toggle),
     * а затем делегировать наверх. Так работает клик и с тача, и с клавиатуры/доступности.
     */
    override fun performClick(): Boolean {
        // Вызовем стандартную реализацию (для accessibility/анимаций звука и т.п.).
        val handledBySuper = super.performClick()
        // Переключим состояние и уведомим слушателя
        toggle()
        // Возвращаем true, так как мы обработали клик
        return true
    }

    private fun updateContentDescription() {
        contentDescription = if (isPlaying)
            context.getString(R.string.cd_pause) else context.getString(R.string.cd_play)
    }
}

/**Опция 2: кешировать Bitmap по текущему размеру =
 * делаем его один раз на размер (в onSizeChanged) и переиспользуем в onDraw.
 * При смене размера — пересоздаваем
 * устраняем “дорогую операцию” из горячего пути:
 * битмапы создаются только при изменении размеров/иконок, а не каждый раз**/
/*
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

    private var isPlaying = false

    // Иконки-источники
    private var playDrawable: Drawable? = null
    private var pauseDrawable: Drawable? = null

    // Кэш отрисованных под нужный размер битмапов
    private var bmpPlay: Bitmap? = null
    private var bmpPause: Bitmap? = null
    private var cachedSizePx: Int = -1

    private val dstRect = RectF()
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    /** Колбэк наружу: пользователь попросил переключить воспроизведение */
    var onToggleRequested: ((Boolean) -> Unit)? = null

    init {
        isClickable = true
        isFocusable = true

        // Чтение атрибутов из XML (иконки play/pause)
        if (attrs != null) {
            val a = context.obtainStyledAttributes(
                attrs, R.styleable.PlaybackButtonView, defStyleAttr, 0
            )
            val playRes = a.getResourceId(R.styleable.PlaybackButtonView_iconPlay, 0)
            val pauseRes = a.getResourceId(R.styleable.PlaybackButtonView_iconPause, 0)
            a.recycle()
            if (playRes != 0 && pauseRes != 0) setIcons(playRes, pauseRes)
        }

        updateContentDescription()
    }

    /** Публичная установка иконок из ресурсов */
    fun setIcons(@DrawableRes playRes: Int, @DrawableRes pauseRes: Int) {
        playDrawable = AppCompatResources.getDrawable(context, playRes)
        pauseDrawable = AppCompatResources.getDrawable(context, pauseRes)

        // Смена источников -> сбрасываем кэш
        clearBitmapCache()

        // Если размер уже известен — сразу соберём кэш
        val size = currentSquareContentSize()
        if (size > 0) rebuildBitmaps(size)

        invalidate()
    }

    /** Установить состояние извне (например, трек завершился) */
    fun setPlaying(playing: Boolean) {
        if (isPlaying != playing) {
            isPlaying = playing
            updateContentDescription()
            invalidate()
        }
    }

    /** Локальное переключение + сигнал наружу */
    private fun toggle() {
        setPlaying(!isPlaying)
        onToggleRequested?.invoke(isPlaying)
    }

    /** Квадратный размер контента (без паддингов) */
    private fun currentSquareContentSize(): Int {
        if (width <= 0 || height <= 0) return 0
        val cw = (width - paddingLeft - paddingRight).toFloat()
        val ch = (height - paddingTop - paddingBottom).toFloat()
        return cw.coerceAtMost(ch).toInt().coerceAtLeast(0)
    }

    /** Пересобрать кэш под новый размер, если нужно */
    private fun rebuildBitmaps(targetSize: Int) {
        if (targetSize <= 0) return
        if (targetSize == cachedSizePx && bmpPlay != null && bmpPause != null) return

        cachedSizePx = targetSize
        bmpPlay = renderToBitmap(playDrawable, targetSize, targetSize)
        bmpPause = renderToBitmap(pauseDrawable, targetSize, targetSize)
    }

    private fun clearBitmapCache() {
        bmpPlay = null
        bmpPause = null
        cachedSizePx = -1
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        // Центрируем квадрат и запоминаем целевой прямоугольник
        val cw = (w - paddingLeft - paddingRight).toFloat()
        val ch = (h - paddingTop - paddingBottom).toFloat()
        val size = cw.coerceAtMost(ch)
        val left = paddingLeft + (cw - size) / 2f
        val top = paddingTop + (ch - size) / 2f
        dstRect.set(left, top, left + size, top + size)

        // Под новый размер — новый кэш
        rebuildBitmaps(size.toInt())

        invalidate()
    }

    @SuppressLint("UseKtx")
    private fun renderToBitmap(drawable: Drawable?, w: Int, h: Int): Bitmap? {
        if (drawable == null || w <= 0 || h <= 0) return null
        val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val c = Canvas(bmp)

        // Прокидываем состояние/видимость (pressed/disabled и т.п.), если drawable это поддерживает
        drawable.state = drawableState
        drawable.setVisible(isShown, false)

        drawable.setBounds(0, 0, w, h)
        drawable.draw(c)
        return bmp
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Ленивое построение кэша (на случай, если setIcons был до измерения)
        if (bmpPlay == null || bmpPause == null) {
            val size = currentSquareContentSize()
            if (size > 0) rebuildBitmaps(size)
        }

        paint.isFilterBitmap = true // мягкое масштабирование при необходимости
        val b = if (isPlaying) bmpPause else bmpPlay
        b?.let { canvas.drawBitmap(it, null, dstRect, paint) }
    }

    /** Каноничный обработчик касаний: UP внутри -> performClick() */
    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                isPressed = true
                return true // захватываем жест
            }
            MotionEvent.ACTION_UP -> {
                isPressed = false
                val inside = event.x in 0f..width.toFloat() && event.y in 0f..height.toFloat()
                if (inside) return performClick()
            }
            MotionEvent.ACTION_CANCEL -> {
                isPressed = false
            }
        }
        return super.onTouchEvent(event)
    }

    /** performClick: вызываем логику клика + даём системе обработать клик (accessibility/feedback) */
    override fun performClick(): Boolean {
        super.performClick()
        toggle()
        return true
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        // Освобождаем ссылки, чтобы GC быстрее прибрал память
        clearBitmapCache()
    }

    private fun updateContentDescription() {
        contentDescription = if (isPlaying)
            context.getString(R.string.cd_pause) else context.getString(R.string.cd_play)
    }
}
*/